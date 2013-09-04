/* == timetree.js: Data structures for a time-varying shallow tree of strings ================== */

TimeTreeDisplay = function(div, customRenderMethod) {
	var ttd = 
	{
	        getOutputString: function(thread) {
	            if(customRenderMethod)
	                return customRenderMethod(thread);
	            return "<h3>" + thread.firstLine + "</h3><pre>" + thread.dump + "</pre>";
	        },

		/* populate wants an array of thread objects, where a thread object is:
		 * { id: string, dump: string}.
		 * So eg., threads = [{firstLine:_,dump:_},{firstLine:_,dump:_},...,{firstLine:_,dump:_}].
		 */
		populate: function(threads) {
			var isArray = typeof(threads) == "object" && threads && threads instanceof Array;
			if (!isArray) return;
			for (var i = 0; i < threads.length; i++) {
				var thread = threads[i];
				if (typeof(thread.firstLine) == "undefined" ||
						typeof(thread.dump) == "undefined") {
					return;
				}
			}
			/** TODO? Some sorting here. **/
			this._tree.populate(threads);
		},
		
		/* getAll returns an array of arrays of maps of all the threads.
		 */
		getAll: function() {
			return this._tree._series;
		},
		
		/* openThreadListWindow
		 */
		openThreadListWindow: function() {
			var dumpWindow = window.open("","Long running threads");
			dumpWindow.document.open("text/plain");
			var times = this.getAll();
			for (var i = 0; i < times.length; i++) {		
			    var threads = times[i];
			    for (var j = 0; j < threads.length; j++) {
			        var thread = threads[j];      
			        dumpWindow.document.write(thread["dump"]);
				}
			}
			dumpWindow.document.close();
		},

		/* afterPopulate responds to the event of someone populating the tree with data.
		 * Called from the observed tree (this._tree).
		 */
		afterPopulate: function(tt) {
			this.updateCounter(tt);
			if (this._track) {
				this._tree.latest();
			}
			if (this._first) {
				this.redraw(tt);
				this._first = false;
			} 
		},
		
		/* afterMove responds to the event of someone navigating in the underlying tree.
		 * Called by the observed tree (this._tree).
		 */
		afterMove: function(tt) {
			this.updateCounter(tt);
			this.redraw(tt);
		},
		
		updateCounter: function(tt) {
			var counter =  tt.length() > 0 ? tt.index() + 1 + "/" + tt.length() : "0/0";
			$(this._div + " .toolbar .counter").html(counter);
		},
		
        redraw: function(tt) {
            var threads = tt.tree();
            $(this._div + " .treeview").html("");
            if (typeof(threads) == "undefined") {
                return;
            }
            var e = this;
            
            for (var i = 0; i < threads.length; i++) {
                var colorClass = i % 2 == 0 ? "even" : "odd";
                var thread = threads[i];
                var output = this.getOutputString(thread);
                $(this._div + " .treeview").append("<li class='"+colorClass+"'>" + output + "</li>");
            }
            $(this._div + " .treeview h3").click(function(){ 
                $(this).parent().children("pre").slideToggle(150);
            });
        },
		
		init: function() {
			this._tree = new TimeTree(this);
			var e = this;
			
			$(this._div).html(
				  '<div class="toolbar"><div class="track"><input type="checkbox" id="trackreqs" />' 
				+ '<label for="trackreqs"> Always show latest</label></div><div>'
				+ '<div class="previous">&lt;&lt;</div> <div class="next">&gt;&gt;</div>'
				+ '<div class="counter">0/0</div><div class="showall">Show all</div>'
				+ '</div></div>'
				+ '<ul class="treeview"></div>');
				
			$(this._div + " .toolbar .next").click(function() { e._tree.next(); });
			$(this._div + " .toolbar .previous").click(function() { e._tree.previous(); });
			$(this._div + " .toolbar .showall").click(function() { e.openThreadListWindow(); });
			$(this._div + " .toolbar .track input").change(function() { e._track = $(this).attr("checked"); });
			$(this._div + " .toolbar div").map(function() { this.onselectstart = function() { return false; }  });
			return this;
		},
		_tree: null,
		_track: false,
		_div:  div,
		_first: true,
	};

	return ttd.init();
}

/* ==============================================================================================
	f = new TimeTree();     // At time 0
	f.populate(t0);         // == f
	f.populate(t1);         // == f
	f.length();             // == 2
	f.tree();               // == t0
	f.forward();            // == f
	f.tree();               // == t1
	f.backward().backward() // == f
 * ============================================================================================== */
TimeTree = function(obs) {
	var o = obs;
	if (o == null) {
		o = function() 
		{
			return {
				afterPopulate: function(tree) {},
				afterMove: function(tree) {}
			}
		}
	}
	
	return {
		populate: function(tree) {
			this._series.push(tree);
			o.afterPopulate(this);
			return this;
		},
				
		length: function() {
			return this._series.length;
		},
		
		tree: function() {
			if (this._idx >= 0 && this._idx < this.length()) {
				return this._series[this._idx];
			}
		},
		
		index: function() {
			return this._idx;
		},
		
		next: function() {
			this._idx = this.length() <= 0 ? 0 : (++this._idx % this.length());
			o.afterMove(this);
			return this;
		},
		
		latest: function() {
			this._idx = this.length() <= 0 ? 0 : this.length()-1;
			o.afterMove(this);
			return this;
		},
		
		previous: function() {
			if (this.length() > 0) {
				this._idx = (this._idx > 0 ? this._idx : this.length()) - 1;
			}
			o.afterMove(this);
			return this;
		},
		
		_idx: 0,
		_series: [],
		_observer: o
	}
}