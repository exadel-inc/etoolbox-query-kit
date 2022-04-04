(function ($, ns) {
    'use strict';

    const TABLE_RESULTS_ID = '#resultsTable';
    const COL_STYLES_ID = 'colStyles';
    const RESIZER_CLASS = 'resizer';

    $(`<style id="${COL_STYLES_ID}">`).prependTo(document.head);
    const $savedColStyles = $(`#${COL_STYLES_ID}`);

    ns.initResizableCols = () => {
        $(`${TABLE_RESULTS_ID} table colgroup`).remove();

        $(`${TABLE_RESULTS_ID} coral-table-headercell-content`).each((i, coralCell) => {
            const $resizer = $(`<div class="${RESIZER_CLASS}">`);
            $resizer.appendTo($(coralCell));
            createResizableColumn($(coralCell), $(coralCell).closest('th'), $resizer);
        });
    };

    const createResizableColumn = function ($coralCell, $col, $resizer) {
        let x = 0;
        let curColWidth = 0;
        let nextColWidth = 0;

        const $nextCol = $col.next();

        const curColMinWidth = parseFloat($col.css('min-width')) || Number.POSITIVE_INFINITY;
        const nextColMinWith = parseFloat($nextCol.css('min-width')) || Number.POSITIVE_INFINITY;
        const curColPadding = getPadding($col);
        const nextColPadding = getPadding($nextCol);

        const mouseDownHandler = function (e) {
            x = e.clientX;

            curColWidth = parseFloat($col.css('width'));
            if ($nextCol.length) {
                nextColWidth = parseFloat($nextCol.css('width'));
            }

            $(document).on('mousemove', mouseMoveHandler);
            $(document).on('mouseup', mouseUpHandler);
        };

        const mouseMoveHandler = (e) => requestAnimationFrame(() => {
            $savedColStyles.text('');

            const dx = e.clientX - x;

            if (curColWidth + dx + curColPadding < curColMinWidth) return;
            if ($nextCol.length && nextColWidth - dx + nextColPadding < nextColMinWith) return;

            setWidth($col, curColWidth + dx, curColPadding);
            $nextCol.length && setWidth($nextCol, nextColWidth - dx, nextColPadding);
        });

        const mouseUpHandler = function () {
            $(document).off('mousemove', mouseMoveHandler);
            $(document).off('mouseup', mouseUpHandler);

            saveColStyles();
        };

        $resizer.on('mousedown', mouseDownHandler);
    };

    function saveColStyles() {
        const thSelector = `${TABLE_RESULTS_ID} th`;
        let result = '';

        $(thSelector).each((i, col) => {
            result += `${thSelector}:nth-child(${i + 1}) { 
            width: ${$(col).css('width')};
            }`;

            result += `${thSelector}:nth-child(${i + 1}) coral-table-headercell-content { 
            width: ${$(col).children().first().css('width')} !important;
            }`;
        });

        $savedColStyles.text(result);
    }

    function setWidth($col, width, padding) {
        $col.css('width', width);
        const $coralCell = $col.children('coral-table-headercell-content').first();
        $coralCell.css('width', width + padding);
    }

    function getPadding($col) {
        if ($col.css('box-sizing') === 'border-box') {
            return 0;
        }

        const padLeft = $col.css('padding-left');
        const padRight = $col.css('padding-right');
        return (parseFloat(padLeft) + parseFloat(padRight));
    }
})(Granite.$, Granite.Eqk = (Granite.Eqk || {}));
