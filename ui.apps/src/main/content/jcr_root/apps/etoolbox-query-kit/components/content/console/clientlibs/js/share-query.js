"use strict"

$(document).ready(function () {

    var $shareButton = $('#shareButton'),
        $queryForm = $('#queryForm'),
        $languageSelect = $('#languageSelect')[0],
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

    function updateQueryFromUrlAndSubmit(){
        var urlParams = decodeQueryUrlParams();
        var editor = document.querySelector('.CodeMirror').CodeMirror;
        if (validateQueryLanguageParam(urlParams.language) && urlParams.query){
           $languageSelect.items.getAll().forEach(function(item, idx){
             if(item.value === urlParams.language){
               item.selected = true;
             }
           });
           editor.setValue(urlParams.query);
           $executeButton.trigger( "click" );
        } else {
           console.info("URL params isn't valid.")
        }
    }

    function validateQueryLanguageParam(language) {
        if (language === "XPath" || language === "SQL2") {
            return language;
        }
        return null;
    }

   function decodeQueryUrlParams() {
       var urlParameters = getUrlParameters();
       return {
            language: urlParameters["language"],
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
        var language = $languageSelect.selectedItem.value;
        var query = editor.getValue();
        successTooltip.open = true;
        if (query) {
            var urlWithoutParams = window.location.origin + window.location.pathname;
            navigator.clipboard.writeText(urlWithoutParams + '?language=' + language + '&query=' + encodeURIComponent(query));
            setTimeout(function(){
                successTooltip.open = false;
            }, 2000)
        }
    }));

    setTimeout(function() {
      updateQueryFromUrlAndSubmit();
    }, 0);
});