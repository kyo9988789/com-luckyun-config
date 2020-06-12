/* eslint-disable no-underscore-dangle */
import React, { PureComponent } from 'react';
import PropTypes from 'prop-types';
import { GridTable } from 'luck-react';

class GridList extends PureComponent {
  static propTypes = {
    paginationOnly: PropTypes.bool,
    enableRangeSelection: PropTypes.bool,
  };

  static defaultProps = {
    paginationOnly: true,
    enableRangeSelection: true,
  };

  handlePageChange = (pageno, pagesize) => {
    const {
      modelName,
      paginationOnly,
      pagination: { onChange },
    } = this.props;
    if (paginationOnly) {
      window.g_app._store.dispatch({
        type: `${modelName}/doQuery`,
        payload: { pageno, pagesize },
      });
    }
    if (onChange) onChange(pageno, pagesize);
  };

  handleShowSizeChange = (pageno, pagesize) => {
    const {
      modelName,
      paginationOnly,
      pagination: { onShowSizeChange },
    } = this.props;
    if (paginationOnly) {
      window.g_app._store.dispatch({
        type: `${modelName}/doQuery`,
        payload: { pageno, pagesize },
      });
    }
    if (onShowSizeChange) onShowSizeChange(pageno, pagesize);
  };

  showTotal = total => `共${total || 0}条`;

  render() {
    const {
      rowKey='indocno',
      className,
      columns,
      dataSource,
      pagination,
      showStatusBar,
      loading,
      rowSelection,
      batchOperations,
      paginationOnly,
      defaultColDef,
      enableRangeSelection,
      renderTopLeft,
      renderTopRight,
      renderActions,
      renderIndex,
      renderExpandedRow,
      onBatchOperationsClick,
      ...rest
    } = this.props;

    let paginationProps = false;
    if (pagination) {
      const {
        total,
        showSizeChanger,
        showQuickJumper,
        hideOnSinglePage,
        ...paginationRest
      } = pagination;
      paginationProps = {
        ...paginationRest,
        total,
        onChange: this.handlePageChange,
        onShowSizeChange: this.handleShowSizeChange,
        showTotal: this.showTotal,
        showSizeChanger: showSizeChanger || true,
        showQuickJumper: showQuickJumper || true,
        hideOnSinglePage: hideOnSinglePage || false,
      };
    }

    return (
      <GridTable
        rowKey={rowKey}
        className={className}
        width="auto"
        columns={columns}
        dataSource={dataSource}
        rowSelection={rowSelection}
        renderTopLeft={renderTopLeft}
        renderTopRight={renderTopRight}
        renderActions={renderActions}
        renderExpandedRow={renderExpandedRow}
        renderIndex={renderIndex}
        loading={loading}
        batchOperations={batchOperations}
        showStatusBar={showStatusBar}
        paginationOnly={paginationOnly}
        pagination={paginationProps}
        onBatchOperationsClick={onBatchOperationsClick}
        defaultColDef={{ ...defaultColDef, resizable: true }}
        enableRangeSelection={enableRangeSelection}
        {...rest}
      />
    );
  }
}

export default GridList;
