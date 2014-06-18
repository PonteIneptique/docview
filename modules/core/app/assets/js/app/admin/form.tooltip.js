// Add Bootstrap tooltip on input boxes with a title.
// Filter items with an empty title.
define(["jquery"], function($) {
  $("input[type=text][title!=''],textarea[title!='']").each(function() {
      var that = $(this);
      that.attr("data-content", that.attr("title"));
      that.attr("title", that.parents(".control-group").find(".control-label").text());
      if(!that.parents(".control-group").hasClass("quiet")) {
        that.popover({
          html: true,
          delay:{
            show: 500,
            hide: 100
          },
          trigger: "blur",
          placement: "bottom"
        });
      }
  });
});