define(["jquery"], function($) {
  $("select.select2:not(.inline-element-template select.select2)").select2();

  // Delete inline date period tables
  $(".inline-element-list").on("click", ".remove-inline-element", function(event) {
    event.preventDefault();
    $(this).parents(".inline-element").first().remove();
  });

  $(".add-inline-element").on("click", function(event) {
    event.preventDefault();
    var container = $(event.target).closest(".inline-formset");
    var set = container.children(".inline-element-list");
    var prefix = container.data("prefix");
    if (!prefix) {
      throw "No prefix found for formset";
    }
    var template = $(".inline-element-template", container);
    var idx = set.children().length;
    // We want to replace all instances of prefix[IDX] and prefix_IDX
    var re1 = new RegExp(prefix + "\\[IDX\\]", "g");
    var re2 = new RegExp(prefix + "_IDX", "g");
    var elem = $(template.html()
        .replace(re1, prefix + "[" + idx + "]")
        .replace(re2, prefix + "_" + idx));
    //container.append(elem);
    set.append(elem);

    // Add select2 support...
    elem.find("div.select2-container").remove()
    elem.find("select.select2").removeClass(".select2-offscreen").select2();
  });
});