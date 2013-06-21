function reloadGrid(gridId, gridData){
    $(gridId).jqGrid('clearGridData');
    $(gridId).jqGrid('setGridParam', {data: gridData}).trigger('reloadGrid');
}
function showError(id, errorData){
    $(id).html(errorData.responseText);
}
