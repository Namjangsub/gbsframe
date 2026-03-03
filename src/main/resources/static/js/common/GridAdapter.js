(function (global) {
  'use strict';

  function getGridCtor() {
    if (!global.tui || !global.tui.Grid) {
      throw new Error('TOAST UI Grid not found. Load tui-grid.min.js before GridAdapter.');
    }
    return global.tui.Grid;
  }

  function wrap(grid) {
    return {
      grid: grid,
      setData: function (rows) {
        grid.resetData(rows || []);
      },
      getData: function () {
        return grid.getData();
      },
      appendRow: function (row) {
        return grid.appendRow(row);
      },
      removeRow: function (rowKey) {
        return grid.removeRow(rowKey);
      },
      on: function (eventName, handler) {
        grid.on(eventName, handler);
      },
      off: function (eventName, handler) {
        if (handler) {
          grid.off(eventName, handler);
        } else {
          grid.off(eventName);
        }
      },
      setColumnWidth: function (name, width) {
        return grid.setColumnWidth(name, width);
      },
      setColumnVisible: function (name, visible) {
        return grid.setColumnVisible(name, visible);
      },
      refreshLayout: function () {
        return grid.refreshLayout();
      },
      destroy: function () {
        grid.destroy();
      }
    };
  }

  function create(el, columns, options) {
    if (!el) {
      throw new Error('GridAdapter.create: el is required.');
    }

    var Grid = getGridCtor();
    var opts = options || {};
    var config = Object.assign({}, opts);
    config.el = el;
    config.columns = columns || [];

    var grid = new Grid(config);
    return wrap(grid);
  }

  global.GridAdapter = {
    version: '1.0.0',
    create: create,
    wrap: wrap
  };
})(window);
