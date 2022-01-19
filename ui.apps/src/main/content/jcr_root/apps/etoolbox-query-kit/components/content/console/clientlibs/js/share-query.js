"use strict"

$(document).ready(function () {

    var $shareButton = $('#shareButton'),
        $queryForm = $('#queryForm'),
        $executeButton = $('#executeButton');

    var successTooltip = new Coral.Tooltip().set({
      content: {
        innerHTML: 'URL copied to clipboard'
      },
      variant: 'success',
      target: '#shareButton',
      placement: 'bottom',
      interaction: 'off'
    });

    $shareButton.append(successTooltip);

    function updateQueryFromUrl() {
        var urlParams = decodeQueryUrlParams();
        var editor = document.querySelector('.CodeMirror').CodeMirror;
        if (urlParams.query && urlParams.query.trim().length > 0){
           editor.setValue(urlParams.query);
        }
    }

   function decodeQueryUrlParams() {
       var urlParameters = getUrlParameters();
       return {
            query: urlParameters["query"]
       }
   };

    function getUrlParameters() {
        var urlParameters = {};
        var query = window.location.search.substring(1);
        var vars = query.split("&");
        for (var i = 0; i < vars.length; i++) {
          var pair = vars[i].split("=");
          try {
            urlParameters[pair[0]] = decodeURIComponent(pair[1]);
          } catch (e) {
            console.log("Param of url is not correct.");
            urlParameters[pair[0]] = "";
          }
        }
        return urlParameters;
    }

    $shareButton.on('click', (function () {
        var editor = document.querySelector('.CodeMirror').CodeMirror;
        var query = editor.getValue();
        if (query) {
            successTooltip.open = true;
            var urlWithoutParams = window.location.origin + window.location.pathname;
            navigator.clipboard.writeText(urlWithoutParams  + '?query=' + encodeURIComponent(query));
            setTimeout(function(){
                successTooltip.open = false;
            }, 2000)
        }
    }));

    setTimeout(function() {
      updateQueryFromUrl();
    }, 0);
});