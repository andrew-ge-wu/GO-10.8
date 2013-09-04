polopoly.service.blog = {
	init: function() {
		var blog = jQuery('#blog');

		if (blog && polopoly.user.isLoggedIn()) {
			var blogId = jQuery("#blogIdForm input[name='blogId']").val();
			var blogs = polopoly.user.getSessionServiceSettings("ubg", "0");
			for (var i = 0; blogs && i < blogs.length; i++) {
				if (blogId == blogs[i]) {
					jQuery("#blog .requiresBlogOwner").css("display", "block");
					return;
				}
			}
		}
	},
	validateBlogForm: function(localizedMessages) {
		  var validation = {
	        rules: {
	          blog_name: {
	            required: true
	          },
	          blog_address: {
	            required: true
	          }
	        },
	        messages: {
	          blog_name: {
	            required: localizedMessages['blog_name:required']
	          },
	          blog_address: {
	            required: localizedMessages['blog_address:required']
	          }
	        },
	        submitHandler: function(form) {
	            jQuery("#submitBlogFormButton").css("background-color", "#ccc");
	            jQuery("#submitBlogFormButton").attr("disabled", true);
                    polopoly.util.injectCSRFToken(form);
	            form.submit();
	        }
	      };

	      var validator = jQuery("#createBlogForm").validate(validation);

	},
    confirm: function(target, message) {
        polopoly.util.injectCSRFToken(target);
        return confirm(message);
    }
};
