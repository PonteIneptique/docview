/**
 * Markdown helper
 */
define(["jquery"], function($) {
   $(".markdown-helper").popover({
      html: true,
      placement: "left",
      content : function () {
        return $(".markdown-cheatsheet").html();
      }
    });
});