"use strict"

$(function () {
    $('#constructorSelect button[coral-multifield-add]')[0].innerText = 'New column';
    $(document).on("click", "#constructorSelect button[coral-multifield-add]", function () {
        var selectorType = $('#constructorFrom coral-radio[checked]')[0].getAttribute('value');
        if (selectorType === 'join') {
            return;
        }
        $('.selectorSelect').hide();
    });
    setTimeout(function () {
        $('.selectorSelect').hide();
    }, 25);


    $('#joinFrom').hide();


    var andButton = $('#constructorWhere button[coral-multifield-add]')[0];
    andButton.innerText = 'AND';

    var orButton = andButton.cloneNode(true);
    orButton.innerText = 'OR'

    $('#constructorWhere')[0].appendChild(orButton);

    var queryConstructorForm = $('#queryConstructor');

    queryConstructorForm.submit(function (e) {
        e.preventDefault();
        var form = $(this);
        var url = form.attr('action');
        $.ajax({
            type: "POST",
            url: url,
            data: form.serialize(),
            success: function(data) {
                var editor = document.querySelector('.CodeMirror').CodeMirror;
                editor.setValue(data);
            }
        });
    })
});

(function ($, $document) {
    $(window).on("load", function() {
        $('#constructorWhere button[coral-multifield-add]')[0].click();
        setTimeout(function () {
            $('coral-buttongroup')[0].hide();
        }, 50);
    });
})($, $(document));

(function(document, $) {

    var propertyNameNeed = ['simpleSearch', 'fullTextSearch', 'sameNode', 'childNode', 'descendantNode', 'length']
    var expressionNeed = ['simpleSearch', 'length', 'localName', 'name', 'fullTextSearch']
    $(document).on("click", "#constructorWhere button[coral-multifield-add]", function () {
        var multifieldWhereLength = $('#constructorWhere')[0].items.length;
        if (multifieldWhereLength < 2) {
            return;
        }
        var pressedButton = this.innerText.toLowerCase();
        setTimeout(function () {
            var buttonGroup = $('#constructorWhere')[0].items.getAll()[multifieldWhereLength - 1].querySelector('coral-buttongroup');

            buttonGroup.items.getAll().forEach(function(item, idx){
                    if(item.value === pressedButton){
                        item.selected = true;
                    }
                });
        }, 100);
    });

    $(document).on("change", ".constraintWhere", function() {
        var multifieldItem = this.closest('coral-multifield-item');
        var propertyNameWhere = multifieldItem.querySelector('#propertyNameWhere');
        propertyNameWhere.show();
        var operatorWhere = multifieldItem.querySelector('#operatorWhere');
        operatorWhere.show();
        var expressionWhere = multifieldItem.querySelector('#expressionWhere');
        expressionWhere.show();

        var selectedValue = $(this).context.selectedItem.value;
        if (selectedValue !== 'simpleSearch') {
            operatorWhere.hide();
        }
        if (propertyNameNeed.indexOf(selectedValue, 0) === -1) {
            propertyNameWhere.hide();
        }
        if (expressionNeed.indexOf(selectedValue, 0) === -1) {
            expressionWhere.hide();
        }
    });

    $(document).on("change", "#constructorWhere", function () {
        setTimeout(function () {
            $('.constraintWhere').each(function (idx, item) {
                item.items.getAll().forEach(function (button, ind) {
                    button.setAttribute('variant', 'minimal')
                });
            })
        }, 25);
    });

    $(document).on("change", "#operatorWhere", function () {
        var multifieldItem = this.closest('coral-multifield-item');
        var expressionWhere = multifieldItem.querySelector('#expressionWhere');
        expressionWhere.show();
        var selectedValue = $(this).context.selectedItem.value;
        if (selectedValue === 'null' || selectedValue === 'nullEmpty' || selectedValue === 'notNull' || selectedValue === 'notEmpty') {
            expressionWhere.hide();
        }
    });

    $(document).on("change", "#constructorFrom", function () {
        var value = this.querySelector('coral-radio[checked]').getAttribute('value');
        var join = $('#joinFrom');
        var selector = $('#selectorFrom');
        var selectorsSelects = $('.selectorSelect');
        if (value === 'selector') {
            join.hide();
            selector.show();
            selectorsSelects.hide();
        } else {
            join.show();
            selector.hide();
            $('.joinConditionFields').hide();
            $(`.childNodeJoinCondition`).show();
            selectorsSelects.show();
        }
    });

    $(document).on("change", "#joinCondition", function () {
        var value = this.querySelector('coral-radio[checked]').getAttribute('value');
        $('.joinConditionFields').hide();
        $(`.${value}`).show();
    });

    $(document).on("change", ".selector-name", function () {
        var selectors = [];
        $('.selector-name').each(function (idx, item) {
            selectors.push(item.value)
        })
        var selectSelectors = $('.selectorSelect');
        selectSelectors.each(function (idx, item) {
            item.items.clear();
            selectors.forEach(function (selector, idx) {
                var selectItem = new Coral.Select.Item();
                selectItem.innerHTML = selector;
                selectItem.value = selector;
                item.items.add(selectItem);
            })
        });
    });

})(document,Granite.$);