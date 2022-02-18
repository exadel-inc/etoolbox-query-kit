(function(window, document, $, URITemplate) {
    "use strict";

    var ns = "." + Date.now();

    function resolveToggleable(control, src, target) {
        if (src) {
            var promise;
            promise = $.ajax({
                url: src,
                cache: false
            });
            return promise.then(function(html) {
                return $(html)
                    .on("foundation-toggleable-hide", function(e) {
                        var target = $(e.target);

                        requestAnimationFrame(function() {
                            target.detach();
                        });
                    })
                    .appendTo(document.body)
                    .trigger("foundation-contentloaded");
            });
        }
        var el;
        if (target) {
            el = $(target);
        } else {
            el = control.closest(".foundation-toggleable");
        }
        return $.Deferred().resolve(el).promise();
    }

    $(window).adaptTo("foundation-registry").register("foundation.collection.action.action", {
        name: "foundation.dialog.query",
        handler: function(name, el, config) {
            var control = $(el);
            var target = config.data.target;
            var nesting = config.data.nesting;
            var src;
            if (config.data.src) {
                src = config.data.src;
            }
            resolveToggleable(control, src, target).then(function(toggleable) {
                var api = toggleable.adaptTo("foundation-toggleable");
                toggleable.off(ns).one("foundation-form-submitted" + ns, function(e, success, xhr) {
                    if (!success) {
                        return;
                    }
                    api.hide();
                });
                requestAnimationFrame(function() {
                    if (nesting === "hide") {
                        var parentAPI = control.closest(".foundation-toggleable").adaptTo("foundation-toggleable");
                        if (parentAPI) {
                            parentAPI.hide();
                        }
                    }
                    api.show(el);
                });
            });
        }
    });
})(window, document, Granite.$, Granite.URITemplate);