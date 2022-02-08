(function(document, $) {
    "use strict";

    var settings = {
        componentField: ["/apps"],
        templateField: ["/conf"],
        contentField: ["/content"],
        resultNumberField: 10
    };

    var settingsTemp = {};

    var components = {};
    var selectList;

    $(document).on("click", "#settingsButton", function() {
        openDialog('#settingsDialog');
    });

    $(document).on("coral-overlay:beforeopen", "#settingsDialog", function() {
        populateSettings();
    });

    $(document).on("coral-collection:add", ".settings-multifield", function() {
        var id = this.id;
        this.items.getAll().forEach(function (item, i) {
            var input = item.querySelector('input')
            if (input && settingsTemp[`${id}`].length > i) {
                input.value = settingsTemp[`${id}`][i];
            }
        })
    });

    $(document).on("click", "#saveSettingsButton", function() {
        collectSettingsFromDialog();
        closeDialog('#settingsDialog')
    });

    function populateSettings() {
        var settingValues;
        var settingField;
        Object.keys(settings).forEach(function (settingKey, i) {
            settingValues = getSettingFromLocalStorage(settingKey);
            settingField = $(`#${settingKey}`)[0];
            settingsTemp[settingKey] = settingValues;
            if (settingField.nodeName === 'CORAL-MULTIFIELD') {
                settingField.items.clear();
                settingValues.forEach(function (value, i) {
                    settingField.items.add();
                })
            } else {
                settingField.value = settingValues;
            }
        });
    }

    function getSettingFromLocalStorage(key) {
        var storageItem = localStorage.getItem(key);
        return storageItem ? JSON.parse(storageItem) : settings[`${key}`];
    }

    function collectSettingsFromDialog() {
        Object.keys(settings).forEach(function (settingKey, i) {
            var values = [];
            $(`#${settingKey} input[is="coral-textfield"]`).each(function (i, value) {
                values.push(value.value);
            });
            saveSettingToLocalStorage(settingKey, values);
        });
    }

    function saveSettingToLocalStorage(key, value) {
        localStorage.setItem(key, JSON.stringify(value));
    }

    $(document).on("click", ".new-option-dialog", function() {
        var dialogId = this.getAttribute('id').replace('Button', 'Dialog')
        openDialog(`#${dialogId}`);
    });

    $(document).on("click", "#editorComponentsButton", function() {
        closeDialog('#componentsDialog');
        editComponentQuery()
    });

    $(document).on("click", "#executeComponentsButton", function() {
        closeDialog('#componentsDialog');
        editComponentQuery();
        $('#executeButton')[0].click();
    });

    $(document).on("click", "#editorTemplateButton", function() {
        closeDialog('#templateDialog');
        editTemplateQuery()
    });

    $(document).on("click", "#executeTemplateButton", function() {
        closeDialog('#templateDialog');
        editTemplateQuery();
        $('#executeButton')[0].click();
    });

    $(document).on("click", "#newQueryButton", function() {
        setQueryValue('');
        updateUrlParams();
        $('.resultTable').remove();
        $('.query-kit-pagination').remove();
        $('#resultInfo').remove();
    });

    function updateUrlParams() {
        var newUrl = window.location.origin + window.location.pathname;
        window.history.pushState({path:newUrl},'',newUrl);
    }

    function editComponentQuery() {
        var selectedComponentResourceType = selectList.selectedItem.value;
        var contentPaths = getSettingFromLocalStorage('contentField');
        var descendantNode = concatenateDescendantNode(contentPaths);
        setQueryValue(`SELECT parent.[jcr:content/jcr:title] FROM [cq:Page] AS parent INNER JOIN [nt:base] AS component ON ISDESCENDANTNODE(component,parent) WHERE ${descendantNode} AND component.[sling:resourceType] = '${selectedComponentResourceType}'`);
    }

    function editTemplateQuery() {
        var selectedTemplate = getSelectSelectedValue('#templateSelect');
        var contentPaths = getSettingFromLocalStorage('contentField');
        var descendantNode = concatenateDescendantNode(contentPaths);
        setQueryValue(`SELECT child.[jcr:title], child.[cq:lastModified], child.[cq:lastModifiedBy] FROM [cq:Page] AS parent INNER JOIN [nt:base] AS child ON ISCHILDNODE(child,parent) WHERE ${descendantNode} AND child.[cq:template] = '${selectedTemplate}'`);
    }
    
    function getSelectSelectedValue(selectSelector) {
        return $(selectSelector)[0].selectedItem.value;
    }

    function setQueryValue(value) {
        var editor = document.querySelector('.CodeMirror').CodeMirror;
        editor.setValue(value);
    }

    function closeDialog(dialogSelector) {
        var dialog = document.querySelector(dialogSelector);
        dialog && dialog.hide();
    }

    function openDialog(dialogSelector) {
        var dialog = document.querySelector(dialogSelector);
        dialog.center();
        dialog.show();
    }

    $(document).on("coral-overlay:beforeopen", "#componentsDialog", function() {
        getComponents();
    });

    $(document).on("coral-overlay:beforeopen", "#templateDialog", function() {
        getTemplates();
    });

    function getTemplates() {
        var templatePath = getSettingFromLocalStorage('templateField');
        var descendantNode = concatenateDescendantNode(templatePath);
        $.ajax({
            url: '/services/etoolbox-query-kit/execute',
            type: "POST",
            data: {'query':`SELECT child.[jcr:title] as title FROM [cq:Template] AS parent LEFT OUTER JOIN [cq:PageContent] AS child ON ISCHILDNODE(child,parent) where ${descendantNode}`},
            success: fillTemplatesSelectItems,
            error: function (error) {
                console.log('LOL');
            }
        })
    }
    
    function concatenateDescendantNode(paths) {
        var descendantNode = `ISDESCENDANTNODE(parent, "${paths[0]}")`;
        for (var i = 1; i < paths.length; i++) {
            descendantNode += ` OR ISDESCENDANTNODE(parent, "${paths[i]}")`
        }
        return descendantNode;
    }

    function fillTemplatesSelectItems(data) {
        var templateSelect = $('#templateSelect')[0];
        templateSelect.items.clear();
        if (data && data["data"].length === 0) {
            return;
        }
        data["data"].forEach(function (component, i) {
            var selectItem = new Coral.Select.Item();
            selectItem.innerHTML = component["title"];
            selectItem.value = component["path"].split('/jcr:content').shift();
            templateSelect.items.add(selectItem);
        });
    }

    function getComponents() {
        var componentsPath = getSettingFromLocalStorage('componentField');
        var descendantNode = concatenateDescendantNode(componentsPath);
        $.ajax({
            url: '/services/etoolbox-query-kit/execute',
            type: "POST",
            data: {'query':`SELECT [jcr:title], [componentGroup] FROM [cq:Component] AS parent WHERE ${descendantNode} ORDER BY [componentGroup], [jcr:title]`},
            success: fillComponentsSelectItems,
            error: function (error) {
                console.log('LOL');
            }
        })
    }

    function fillSelectList(prefix) {
        selectList.items.clear();
        selectList.groups.clear();
        Object.keys(components).forEach(function (group, i) {
            var currentGroup = new Coral.SelectList.Group();
            currentGroup.set({
                label: group
            });
            components[group].forEach(function (component, i) {
                if (component["title"].toLowerCase().startsWith(prefix.toLowerCase())) {
                    currentGroup.items.add(new Coral.SelectList.Item().set({
                        value: component["resourceType"],
                        innerHTML: component["title"]
                    }));
                }
            });
            if (currentGroup.items.getAll().length > 0) {
                selectList.items.add(currentGroup);
            }
        });
    }

    function fillComponentsSelectItems(data) {
        var search = new Coral.Search();
        search.set({
            placeholder: "Enter Keyword"
        })
        selectList = new Coral.SelectList();
        components = {};
        data["data"].forEach(function (component, i) {
            var group = component["componentGroup"];
            if (!components[group]) {
                components[group] = [];
            }
            components[group].push({
                title: component["jcr:title"],
                resourceType: component["path"].split('/apps/')[1]
            })
        });

        fillSelectList('');

        search.on("coral-search:input", function() {
            fillSelectList(this.value);
        });

        search.on("coral-search:clear", function() {
            fillSelectList('');
        });

        var container = $('#searchContainer');
        container.empty();
        container[0].prepend(selectList);
        container[0].prepend(search);
    }

})(document, Granite.$);