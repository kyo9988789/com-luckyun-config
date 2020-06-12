import React, { PureComponent } from 'react';
import { Pagination, Empty, Spin } from 'antd';
import { Box, ReactSortable } from 'luck-react';
import styles from './styles/index.less';
import EmptySvg from '@/assets/empty.svg';

const {Paste} = Box.Layout;

class CardList extends PureComponent{

  handlePageChange = (current, pageSize)=> {
    const { modelName, pagination: { onPageChange } } = this.props;
    if (modelName) {
      window.g_app._store.dispatch({
        type: `${modelName}/doQuery`,
        payload: {pageno: current, pagesize: pageSize}
      });
    }
    if (onPageChange) {
      onPageChange(current, pageSize)
    }
  };

  handleShowSizeChange = (current, pageSize)=> {
    const { modelName, pagination: { onShowSizeChange } } = this.props;
    if (modelName) {
      window.g_app._store.dispatch({
        type: `${modelName}/doQuery`,
        payload: {pageno: current, pagesize: pageSize}
      });
    }
    if (onShowSizeChange) {
      onShowSizeChange(current, pageSize)
    }
  };

  render() {
    const { className, style, dataSource, pagination = {total: 0}, renderCard, renderTopLeft, renderTopRight, col=3, draggable, dragHandle, onDragEnd, loading, isFlow = false, cardKey = 'indocno'} = this.props;
    const { total, defaultPageSize, pageSizeOptions, showSizeChanger, showQuickJumper, hideOnSinglePage, ...paginationRest } = pagination;
    const cardDom = dataSource && dataSource.map((item, index) => (
      <Box weight={1} className='canDrag' key={item[cardKey]}>
        {renderCard && renderCard(item, index)}
      </Box>
    ));

    let com =  <div style={{flex: 1, paddingTop: '24px'}}><Empty image={EmptySvg} /></div>;
    if (dataSource && dataSource.length > 0) {
      com = (
        <Paste>
          <ReactSortable
            options={{
              disabled:!draggable,
              draggable: '.canDrag',
              handle: dragHandle||'.handle',
              chosenClass: styles.chosen,
              onEnd: evt => onDragEnd(evt),
            }}
            getDomNode={dom =>dom.childNodes[0]}
            style={{ width: '100%' }}
          >
            <Box wrap='wrap' weightSum={col}>
              {cardDom}
            </Box>
          </ReactSortable>
        </Paste>
      )
    }
    return(
      <div className={`${styles.cardList} ${className || ''}`} style={isFlow ? style : {paddingBottom: 40, ...style}}>
        <Spin spinning={!!loading}>
          <Box direction="column" justify="space-between">
            {
              renderTopLeft || renderTopRight ? (
                <Box direction="row" justify="space-between" className={styles.topBox}>
                  <Paste><>{renderTopLeft && renderTopLeft()}</></Paste>
                  <Paste><>{renderTopRight && renderTopRight()}</></Paste>
                </Box>
              ) : null
            }
            {com}
          </Box>
          {!isFlow && (
            <div className={styles.cardPaginationBox}>
              <Pagination
                total={total}
                defaultPageSize={defaultPageSize || 3 * col}
                showSizeChanger={showSizeChanger || true}
                showQuickJumper={showQuickJumper || true}
                hideOnSinglePage={hideOnSinglePage || false}
                size="small"
                showTotal={(total) => `共 ${total} 条`}
                pageSizeOptions={pageSizeOptions || [String(3 * col), String(6 * col), String(9 * col)]}
                onChange={this.handlePageChange}
                onShowSizeChange={this.handleShowSizeChange}
                {...paginationRest}
                className={pagination.className || ''}
              />
            </div>
          )}
        </Spin>
      </div>
    )
  }
}

export default CardList;