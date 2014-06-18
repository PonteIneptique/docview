/**
* jQuery plugin that makes an element 'stick' to the bottom
* of the viewport if it is outside. Used for form action
* sections containing the submit button.
*/
define(["jquery"], function($) {
  $.fn.stickyFormFooter = function() {
    if(this.length > 0) {
      var that = this;
      var top = that.offset().top;
      var height = that.outerHeight();
      var innerHeight = that.height();

      function shouldStick() {
        var vpend = $(window).outerHeight() + $(window).scrollTop();
        var sticky = top > vpend + innerHeight - height;

        if (sticky) {
          if (!that.isSticky === sticky || ($(window).outerHeight() != that.attr("data-windows"))) {
            that.css({
              position: "fixed",
              left: 0,
              width: $(window).width(),
              top: $(window).height() - height// - 15 // Unfortunate fudge factor!
            }).addClass("sticky").attr("data-windows", $(window).outerHeight());
            that.isSticky = sticky;
          }
        }
        else {
          if (!that.isSticky === sticky) {
            that.removeAttr("style")
                .removeClass("sticky");
            that.isSticky = null;
          }
        }
      }

      $(window).scroll(shouldStick);
      $(window).on('resize', shouldStick);

      shouldStick();
    }
  }

  $(".form-actions").stickyFormFooter();

  $("nav.responsive").stickyFormFooter();
});