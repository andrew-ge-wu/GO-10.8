/* == series.js: Manages analysis of multiple time series ====================================== */
/* Usage instructions
    var analyzer = new SeriesAnalyzer("#div-to-occupy");
	// Batch mode
	analyzer.addSeries("series-a", [1,2,3,4,67,13,7,345234,127,241241,123]);
	// Online mode
	analyzer.updateSeries("series-b", 12);
	analyzer.updateSeries("series-b", 4);
	analyzer.updateSeries("series-b", 8);
	analyzer.updateSeries("series-b", 31);
*/
/* == SeriesAnalyzer =========================================================================== */
SeriesAnalyzer = function(host) {

	$(host).html(
		"<div class='histogram'></div><div style='clear: both'></div>" + 
		"<span class='total-histogram-values'></span>" +
		"<table cellspacing='0'><thead><tr>"+
		"<th class='primaryKey'>Key</th> <th>Samples</th>"+
		"<th>Min</th> <th>Avg</th> <th>Max</th></tr></thead>" +
		"<tbody></tbody></table>"
	);
	

	return {
	_table: host + " table",						// The data table we store series statistics in
	_canvas: host + " .histogram",					// The histogram canvas
	_totlist: host + " .total-histogram-values",	// The maximum values below the histogram
	_series: {},									// The hash for series, a {key => data} map.
	total: {},										// Total statistics.
	_selectedHistogram: null,                       // The currently displayed histogram (null == total)

	addSeries: function(key, data) {

		this.createEmptySeriesIfNeeded(key);
		this._internalAddSeries(key, data);

	},
	
	updateSeries: function(key, data) {
		if (!this.seriesExist(key)) {
			this.createEmptySeriesIfNeeded(key);
			this._internalAddSeries(key, [data]);
		} else {
			this._series[key].statistic.update(data);
			this._series[key].histogram.update(data);
		}
		this.total.statistic.update(data);
		this.total.histogram.update(data);
	},
	
	seriesExist: function(key) {
		return (typeof(this._series[key]) != 'undefined');
	},
	
	_internalAddSeries: function(key, data) {
		var hash = this.calculateSeriesStats(data, key);
		hash.statistic.target = "#" + hash.md5;
		hash.statistic.printer = this.drawTableRowStats(key);
		this._series[key] = hash;
		
		hash.statistic.print();
		var f = this;
		hash.statistic.onClick(function() { f.redraw(key) });
		hash.histogram.onUpdate = function () { f.redrawIfVisible(key); };
	},
	
	createEmptySeriesIfNeeded: function(key) {
		if (typeof(this._series[key]) == 'undefined') {
			this._series[key] = { md5: $.md5(key) };
			this._makeRow(this._series[key]["md5"]);
		}
	},
	remakeTotal: function() {
		allData = [];
		for (k in this._series) {
			allData = allData.concat(this._series[k]["series"]);
		}
		this.total = this.calculateSeriesStats(allData);
		this.drawHistogram(this.total.histogram);
		this.total.statistic.target = this._totlist;
		this.total.statistic.printer = this.drawMaxStats;
		this.total.statistic.print();
		f = this; 
		this.total.statistic.onClick(function() {f.redraw()});
		this.total.histogram.onUpdate = function() { f.redrawIfVisible() };
	},
	
	_makeRow: function(key) {
		var ev = $(this._table + " tbody tr:not(.fullKey):last").hasClass("even");
		$(this._table + " tbody").append("<tr class='" +
		(ev ? "odd" : "even") + "' id='" + key + "'></tr>");
	},
	
	drawTableRowStats: function(key) {
		var maxLength = 50;
		
		var trimedKey = key;
		if (key.length > maxLength) {
		  trimedKey = key.substr(0, maxLength-3) + "...";	
		} 
		
		return function(l, a, ma, mi) {return  "<th>"+trimedKey+"</th> <td>" + l + "</td><td>"+ mi + "</td><td>" + a + "</td><td>"+ ma +"</td>"};
	},
	
	drawMaxStats: function(l,a,ma,mi) {
		return "<span><strong>Total</strong> Samples: " + l + ", Min: " + mi + ", Avg: " + a + ", Max: " + ma + "</span>";
	},	
	redrawIfVisible: function() {
		if (!arguments.length && this._selectedHistogram == null) {
			this.redraw();
		} else if (this._selectedHistogram == arguments[0]) {
			this.redraw(arguments[0]);
		}
	},
	redraw: function() {
		if (!arguments.length) {
			$(this._table + " tbody tr.fullKey").hide();
			this.drawHistogram(this.total.histogram);
			this._selectedHistogram = null;
		} else {
			var key = arguments[0];
			this.drawHistogram(this._series[key].histogram);
			this._selectedHistogram = key;
			$(this._table + " tbody tr.fullKey").hide();
			var fullId = "full-" + this._series[key].md5;
			if ($("#"+fullId).length == 0) {
					$(this._table + " tr#"+this._series[key].md5).after("<tr id='"+fullId+"' class='fullKey'><td colspan='5'>"+key+"</td></tr>");
			} 
			$("#" + fullId).show();
		}
	},
	calculateSeriesStats: function(series) {
		var hash = {};
 		hash.series   = series;
		hash.statistic  = new SeriesStatistic(series);
		hash.histogram  = new SeriesHistogram(series);
		if (arguments.length == 2) {
			 hash.md5 = $.md5(arguments[1]);
		}	
		return hash;
	},
	drawHistogram: function(histogram) {
		var boxHeight = parseInt($(this._canvas).height());
		var boxWidth =  parseInt($(this._canvas).width());
		//alert(boxHeight);
		var histo = this._innerDrawHistogram(histogram, boxHeight);
		$(this._canvas).html("<center>"+histo["html"]+"</center>");
		var histoWidth = parseInt(0.95*boxWidth/histo["columns"]);
		$(this._canvas + " div").css("width",histoWidth+"px");
	},
	/** 
	 * _createHistogramDivs
	 * Draws the actual histogram.
	 */
	_innerDrawHistogram: function(histogram, boxHeight) {
		var sortAndIndex = histogram.asSortedArrayWithReverseIndex();
		var sorted = sortAndIndex.array;
		
		if (sorted.length == 0) {
			return { html: "", columns: 0 };
		}
		
		var pointers = sortAndIndex.index;
		var maxSize = histogram.maxSize();
		var maxIndex = sorted[sorted.length-1][0];
		var scaleFactor = (0.75*boxHeight)/maxSize;
		
		var i = 0, columns = 0, bucket = 1, output = "";

		// We don't want to start rendering before we have a bucket to render in.
		var startedRendering = false;

		while (true) {
			// If we've iterated over all buckets (or we're screwed) terminate
			if (bucket > maxIndex || i > 3000) break;
			var count = 0;
			
			if (typeof(histogram.bins[bucket]) != 'undefined')  {
				count = sorted[pointers[bucket]][1];
				startedRendering = true;
				//alert("bucket is: " + bucket + ", i is" + i + ";  Sorted=" + count + "; Bin=" + histogram.bins[bucket]);
			}
			if (startedRendering) {
				columns++;
				height = parseInt(scaleFactor*count);
				var topValue = boxHeight - height;
				output += ("<div style='height:"+boxHeight+"px'><span>"+bucket + " ms ("+ count +")"+"</span>"+
					"<div style='height:"+height+"px; margin-top:"+topValue+"px' class='bucket-"+bucket+"'></div></div>");
			}
			bucket = 2 << i;
			i++;
			
		}
		return {html: output, columns: columns};
	}
}
}

/* == SeriesHistogram: Running histogram of a time series ====================================== */
SeriesHistogram = function(series) {
	return {
		bins: SeriesHistogram.createHistogram(series),
		maxValue: null, 
		onUpdate: function() {},
		asSortedArrayWithReverseIndex: function() {
			var arr = [];
			for (key in this.bins) {
				arr.push([key, this.bins[key]]);
			}
			sorted = arr.sort(function(a,b) {return a[0] - b[0]});
			pointers = {};
			for (bucket in this.bins) {
				for (key in sorted) {
					if (sorted[key][0] == bucket) {
						pointers[bucket] = key;
					}
				}
			}
			return { array: sorted, index: pointers };
		},
		update: function(newValue) {
			bin = SeriesHistogram.selectBin(newValue);
			this.bins[bin] = typeof(this.bins[bin]) == 'undefined' ? 1 : this.bins[bin] + 1;
			if (this.bins[bin] > this.maxSize()) {
				this.maxValue = this.bins[bin];
			}
			this.onUpdate();
		},
		maxSize: function() {
			if (this.maxValue == null) {
				this.maxValue = this._maxInMap(this.bins);
			}
			return this.maxValue;
		},
		/* Takes the max value of a hash map */
		_maxInMap: function(map) {
			var max = 0;
			for (key in map) {
				if (map[key] > max) {
					max = map[key];
				}
			}
			return max;
		}
	}
}

SeriesHistogram.createHistogram = function(series) {
	var hist = {};
	for (t in series) {
		var p = SeriesHistogram.selectBin(series[t]);
		hist[p] = typeof(hist[p]) == 'undefined' ? 1 : hist[p] + 1;
	}
	return hist;
}

SeriesHistogram.selectBin = function(n) {
	n = n - 1;
	for (i = 1; i <= 64; i++) {
		n = n | (n >> i);
	}
	return n + 1;
}

/* == SeriesStatistic: Keeps track of some statistics of a time series ========================= */
SeriesStatistic = function(series, target) {
	return {
		length:  series.length,
		average: SeriesStatistic.round(SeriesStatistic.average(series)),
		max:     SeriesStatistic.round(Math.max.apply(Math, series)),
		min:     SeriesStatistic.round(Math.min.apply(Math, series)),
		target:  target,
		onClick: function(f) { $(this.target).click(f) },
		printer: function() {},
		print: function() {
			$(this.target).html(this.printer(this.length, this.average, this.max, this.min));
		},
		update: function(newValue) {
			this.length += 1;
			this.average = SeriesStatistic.round(this.average + (newValue - this.average)/this.length);
			this.max = SeriesStatistic.round(Math.max(newValue, this.max));
			this.min = SeriesStatistic.round(Math.min(newValue, this.min));
			this.print();
		}
	}
}

SeriesStatistic.round = function(f) {
	return Math.round(1000*f)/1000;
}

SeriesStatistic.average = function(series) {
	avg = 0;
	for (i = 0; i < series.length; i++) {
		avg = avg + (series[i] - avg)/(i+1);
	}
	return avg;
}

