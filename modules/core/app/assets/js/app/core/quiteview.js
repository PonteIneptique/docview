/*
*   Quiet mode by Thibault Clérice GitHub@PonteIneptique
*/
  
define(["jquery"], function($) {
  $(".form-horizontal .form-group").each(function(e) {
    var $textarea = $(this).find("textarea");
    var $formgroup = $(this);
    if($textarea.length !== "undefined" && $textarea.length === 1 && !$formgroup.hasClass("inline-formset")) {
      $textarea.parent().append('<div><a href="#" class="quiet-toggle"><span class="glyphicon glyphicon-remove"></span><span class="glyphicon glyphicon-fullscreen"></span><span class="Press Esc or Tab to continue"></span></a></div>');
    }
  });
  $(".form-group").on("click", ".quiet-toggle", function(e) {
    $(this).trigger("quiet-toggle")
  });

  $(".form-group").on("quiet-toggle", ".quiet-toggle", function(e) {
    e.preventDefault();
    var $formgroup = $(this).parents(".form-group");
    var $blockhelp = $formgroup.find(".help-block");
    $formgroup.toggleClass("quiet");
    if($formgroup.hasClass("quiet")) {
      $blockhelp.data("html", $blockhelp.html()).html($(".markdown-cheatsheet").html());
      $formgroup.find("textarea").focus();
    } else {
      $blockhelp.html($blockhelp.data("html"));
    }
  });

  $(".form-group").on("keydown", function(e) {
    var $formgroup = $(this);
    if($formgroup.hasClass("quiet")) {
      if (e.keyCode == 27) {
          e.preventDefault();
          $formgroup.find(".quiet-toggle").trigger("quiet-toggle");
      } else if (e.keyCode == 9) {
        e.preventDefault();
        $formgroup.find(".quiet-toggle").trigger("quiet-toggle");

        var $next = $formgroup.nextAll(".form-group:has(.quiet-toggle)").first();
        if($next.length !== "undefined" && $next.length === 1) {
          $next.find(".quiet-toggle").trigger("quiet-toggle");
        } else {
          var $fieldset = $formgroup.parents("fieldset");
          $fieldset.nextAll("fieldset").each(function (e) {
            var $next = $(this).find(".form-group:has(.quiet-toggle)").first();
            if($next.length !== "undefined" && $next.length === 1) {
              $next.find(".quiet-toggle").trigger("quiet-toggle");
              console.log(true);
              return false;
            }
          });
        }
      }
    }
  });
});