(function (document, $) {
    'use strict';

    const settings = {
        componentField: ['/apps'],
        templateField: ['/conf'],
        contentField: ['/content'],
        resultNumberField: 10
    };

    const settingsTemp = {};

    let components = {};
    let selectList;

    $(document).on('click', '#settingsButton', function () {
        openDialog('#settingsDialog');
    });

    $(document).on('coral-overlay:beforeopen', '#settingsDialog', function () {
        populateSettings();
    });

    function populateSettings() {
        let settingValues;
        let settingField;
        Object.keys(settings).forEach(function (settingKey, i) {
            settingValues = getSettingFromLocalStorage(settingKey);
            settingField = $(`#${settingKey}`)[0];
            settingsTemp[settingKey] = settingValues;
            if (settingField.nodeName === 'CORAL-MULTIFIELD') {
                settingField.items.clear();
                settingValues.forEach(function (value, i) {
                    settingField.items.add();
                });
            } else {
                settingField.value = settingValues;
            }
        });
    }

    $(document).on('coral-collection:add', '.settings-multifield', function () {
        const id = this.id;
        this.items.getAll().forEach(function (item, i) {
            const input = item.querySelector('input');
            if (input && settingsTemp[`${id}`].length > i) {
                input.value = settingsTemp[`${id}`][i];
            }
        });
    });

    $(document).on('click', '#saveSettingsButton', function () {
        collectSettingsFromDialog();
        closeDialog('#settingsDialog');
    });

    $(document).on('click', '#newQueryButton', function () {
        setQueryValue('');
        updateUrlParams();
        $('.resultTable').remove();
        $('.query-kit-pagination').remove();
        $('#resultInfo').remove();
    });

    function setQueryValue(value) {
        const editor = document.querySelector('.CodeMirror').CodeMirror;
        editor.setValue(value);
    }

    function updateUrlParams() {
        const newUrl = window.location.origin + window.location.pathname;
        window.history.pushState({ path: newUrl }, '', newUrl);
    }

    $(document).on('click', '.new-option-dialog', function () {
        const dialogId = this.getAttribute('id').replace('Button', 'Dialog');
        openDialog(`#${dialogId}`);
    });

    $(document).on('coral-overlay:beforeopen', '#componentsDialog', function () {
        getComponents();
    });

    function getComponents() {
        const componentsPath = getSettingFromLocalStorage('componentField');
        const descendantNode = concatenateDescendantNode(componentsPath);
        $.ajax({
            url: '/services/etoolbox-query-kit/execute',
            type: 'POST',
            data: { query: `SELECT [jcr:title], [componentGroup] FROM [cq:Component] AS parent WHERE ${descendantNode} ORDER BY [componentGroup], [jcr:title]` },
            success: fillComponentsSelectItems,
            error: function (error) {
                console.log('LOL');
            }
        });
    }

    function fillComponentsSelectItems(data) {
        const search = new Coral.Search();
        search.set({
            placeholder: 'Enter Keyword'
        });
        selectList = new Coral.SelectList();
        components = {};
        for (let i = 0; i < data.results.path.length; i++) {
            const group = data.results.componentGroup[i];
            if (!components[group]) {
                components[group] = [];
            }
            components[group].push({
                title: data.results['jcr:title'][i] ? data.results['jcr:title'][i] : 'No name',
                resourceType: data.results.path[i].split('/apps/')[1]
            });
        }

        fillSelectList('');

        search.on('coral-search:input', function () {
            fillSelectList(this.value);
        });

        search.on('coral-search:clear', function () {
            fillSelectList('');
        });

        const container = $('#searchContainer');
        container.empty();
        container[0].prepend(selectList);
        container[0].prepend(search);
    }

    function fillSelectList(prefix) {
        selectList.items.clear();
        selectList.groups.clear();
        Object.keys(components).forEach(function (group, i) {
            const currentGroup = new Coral.SelectList.Group();
            currentGroup.set({
                label: group
            });
            components[group].forEach(function (component, i) {
                if (component.title.toLowerCase().startsWith(prefix.toLowerCase())) {
                    currentGroup.items.add(new Coral.SelectList.Item().set({
                        value: component.resourceType,
                        innerHTML: component.title
                    }));
                }
            });
            if (currentGroup.items.getAll().length > 0) {
                selectList.items.add(currentGroup);
            }
        });
    }

    $(document).on('coral-overlay:beforeopen', '#templateDialog', function () {
        getTemplates();
    });

    function getTemplates() {
        const templatePath = getSettingFromLocalStorage('templateField');
        const descendantNode = concatenateDescendantNode(templatePath);
        $.ajax({
            url: '/services/etoolbox-query-kit/execute',
            type: 'POST',
            data: { query: `SELECT child.[jcr:title] as title FROM [cq:Template] AS parent LEFT OUTER JOIN [cq:PageContent] AS child ON ISCHILDNODE(child,parent) where ${descendantNode}` },
            success: fillTemplatesSelectItems,
            error: function (error) {
                console.log('LOL');
            }
        });
    }

    function fillTemplatesSelectItems(data) {
        const templateSelect = $('#templateSelect')[0];
        templateSelect.items.clear();
        if (data && data.results.length === 0) {
            return;
        }
        for (let i = 0; i < data.results.path.length; i++) {
            const selectItem = new Coral.Select.Item();
            selectItem.innerHTML = data.results.title[i];
            selectItem.value = data.results.path[i].split('/jcr:content').shift();
            templateSelect.items.add(selectItem);
        }
    }

    function getSettingFromLocalStorage(key) {
        const storageItem = localStorage.getItem(key);
        return storageItem ? JSON.parse(storageItem) : settings[`${key}`];
    }

    function saveSettingToLocalStorage(key, value) {
        localStorage.setItem(key, JSON.stringify(value));
    }

    function collectSettingsFromDialog() {
        Object.keys(settings).forEach(function (settingKey, i) {
            const values = [];
            $(`#${settingKey} input[is="coral-textfield"]`).each(function (i, value) {
                values.push(value.value);
            });
            saveSettingToLocalStorage(settingKey, values);
        });
    }

    $(document).on('click', '#editorComponentsButton', function () {
        closeDialog('#componentsDialog');
        editComponentQuery();
    });

    $(document).on('click', '#executeComponentsButton', function () {
        closeDialog('#componentsDialog');
        editComponentQuery();
        $('#executeButton')[0].click();
    });

    $(document).on('click', '#editorTemplateButton', function () {
        closeDialog('#templateDialog');
        editTemplateQuery();
    });

    $(document).on('click', '#executeTemplateButton', function () {
        closeDialog('#templateDialog');
        editTemplateQuery();
        $('#executeButton')[0].click();
    });

    function editComponentQuery() {
        const selectedComponentResourceType = selectList.selectedItem.value;
        const contentPaths = getSettingFromLocalStorage('contentField');
        const descendantNode = concatenateDescendantNode(contentPaths);
        setQueryValue(`SELECT parent.[jcr:content/jcr:title] FROM [cq:Page] AS parent INNER JOIN [nt:base] AS component ON ISDESCENDANTNODE(component,parent) WHERE ${descendantNode} AND component.[sling:resourceType] = '${selectedComponentResourceType}'`);
    }

    function editTemplateQuery() {
        const selectedTemplate = getSelectSelectedValue('#templateSelect');
        const contentPaths = getSettingFromLocalStorage('contentField');
        const descendantNode = concatenateDescendantNode(contentPaths);
        setQueryValue(`SELECT child.[jcr:title], child.[cq:lastModified], child.[cq:lastModifiedBy] FROM [cq:Page] AS parent INNER JOIN [nt:base] AS child ON ISCHILDNODE(child,parent) WHERE ${descendantNode} AND child.[cq:template] = '${selectedTemplate}'`);
    }

    function getSelectSelectedValue(selectSelector) {
        return $(selectSelector)[0].selectedItem.value;
    }

    function closeDialog(dialogSelector) {
        const dialog = document.querySelector(dialogSelector);
        dialog && dialog.hide();
    }

    function openDialog(dialogSelector) {
        const dialog = document.querySelector(dialogSelector);
        dialog.center();
        dialog.show();
    }

    function concatenateDescendantNode(paths) {
        let descendantNode = `ISDESCENDANTNODE(parent, "${paths[0]}")`;
        for (let i = 1; i < paths.length; i++) {
            descendantNode += ` OR ISDESCENDANTNODE(parent, "${paths[i]}")`;
        }
        return descendantNode;
    }
})(document, Granite.$);
