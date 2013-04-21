var INVALID_CLASS_NAME = /[^0-9a-zA-Z\_\$]/;
var INVALID_PACKAGE_NAME = /[^0-9a-zA-Z\_\$\.]/;

$(document).ready(function() {

  $("#targetpackage").keyup(function(e) {
    if (!$(this).val() || INVALID_PACKAGE_NAME.test($(this).val())) {
      $(this).parents(".control-group").addClass("error");
    } else {
      $(this).parents(".control-group").removeClass("error");
    }
  });

  $("#classname").keyup(function(e) {
    if (!$(this).val() || INVALID_CLASS_NAME.test($(this).val())) {
      $(this).parents(".control-group").addClass("error");
    } else {
      $(this).parents(".control-group").removeClass("error");
    }
  });

  var schemaTextArea = document.getElementById("schema");
  var myCodeMirror = CodeMirror.fromTextArea(schemaTextArea, {
    mode: {name: "javascript", json: true},
    lineNumbers: true,
    matchBrackets: true,
    onChange : function() {
      if (!$("#download-jar-link").hasClass("hide")) {
        $("#download-jar-link").addClass("hide");
      }
    }
  });

  $("#form").submit(function(e) {
    e.preventDefault();
    return false;
  });

  $("#jar-button").click(function(e) {

    $("#jar-button").button("loading");
    $(".alert").alert("close");
    $("#download-jar-link").addClass("hide");
    schemaTextArea.value = myCodeMirror.getValue();

    $.ajax({
      url: "generator",
      type: "POST",
      data: $("#form").serialize(),
      success: function(data) {
        $("#download-jar-link").attr("href", "data:application/zip;base64," + data);
        $("#download-jar-link").attr("download", $("#classname").val() + "-sources.jar");
        $("#download-jar-link").text($("#classname").val() + "-sources.jar");
        $("#download-jar-link").removeClass("hide");

        $("#jar-button").button("reset");
      },
      error: function(xhr) {
        $("#jar-button").button("reset");
        $("#alert-area").prepend($("<div class='alert alert-error fade in' data-alert>" +
                                   "<button type='button' class='close' data-dismiss='alert'>×</button>" +
                                   "<strong>There's a problem:</strong> " + xhr.responseText +
                                   "</div>"));
      }
    });
  });

  $("#preview-button").click(function(e) {

    $("#preview-button").button("loading");
    $(".alert").alert("close");
    schemaTextArea.value = myCodeMirror.getValue();

    $.ajax({
      url: "generator/preview",
      type: "POST",
      data: $("#form").serialize(),
      success: function(data) {
        $("#preview-button").button("reset");
        CodeMirror.runMode(data,
                           "text/x-java",
                           document.getElementById("preview"),
                           {indentUnit:4});
        $("#preview-modal").modal();
      },
      error: function(xhr) {
        $("#preview-button").button("reset");
        $("#alert-area").append($("<div class='alert alert-error fade in' data-alert>" +
                                  "<button type='button' class='close' data-dismiss='alert'>×</button>" +
                                  "<strong>There's a problem:</strong> " + xhr.responseText +
                                  "</div>"));
      }
    });
  });

});
