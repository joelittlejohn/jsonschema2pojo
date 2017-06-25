var INVALID_CLASS_NAME = /[^0-9a-zA-Z\_\$]/;
var INVALID_PACKAGE_NAME = /[^0-9a-zA-Z\_\$\.]/;

$.fn.serializeAndEncode = function() {
  // better than jQuery serialize as it uses %2B for space so is compatible with decodeURIComponent
  return $.map(this.serializeArray(), function(val) {
    return [val.name, encodeURIComponent(val.value)].join('=');
  }).join('&');
};

function saveCookie() {
  dataString = $("#form").serializeAndEncode();
  Cookies.set('formData', dataString, { expires: 365 });
  return false;
}

function restoreCookie() {
  var cookieData = Cookies.get('formData');
  if (cookieData) {
    var cookieArray = cookieData.split('&');

    $('#form input:checkbox').prop("checked", false);

    $.each(cookieArray, function(k, v) {
      var field = v.split('=');
      var fieldName = field[0];
      var fieldValue = decodeURIComponent(field[1]);

      if (fieldName !== "schema") {
        var input = $('#form [name="'+fieldName+'"]');

        if ( input.prop("type") == "radio" ) {
          $('#form [name="'+fieldName+'"][value="' + fieldValue + '"]').prop("checked", true);
        } else if ( input.prop("type") == "checkbox" ) {
          input.prop("checked", true);
        } else {
          console.log(fieldValue);
          input.val(fieldValue);
        }
      }
    });
  }
}

$(document).ready(function() {

  restoreCookie();

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
      if (!$("#download-zip-link").hasClass("hide")) {
        $("#download-zip-link").addClass("hide");
      }
    }
  });

  $("#form").submit(function(e) {
    e.preventDefault();
    return false;
  });

  $("#zip-button").click(function(e) {

    $("#zip-button").button("loading");
    $(".alert").alert("close");
    $("#download-zip-link").addClass("hide");
    schemaTextArea.value = myCodeMirror.getValue();

    $.ajax({
      url: "generator",
      type: "POST",
      data: $("#form").serializeAndEncode(),
      success: function(data) {
        $("#download-zip-link").attr("href", "data:application/zip;base64," + data);
        $("#download-zip-link").attr("download", $("#classname").val() + "-sources.zip");
        $("#download-zip-link").text($("#classname").val() + "-sources.zip");
        $("#download-zip-link").removeClass("hide");

        $("#zip-button").button("reset");
      },
      error: function(xhr) {
        $("#zip-button").button("reset");
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
      data: $("#form").serializeAndEncode(),
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

  $(document).on("click", "#preview-button", saveCookie);
  $(document).on("click", "#zip-button", saveCookie);

});
