import React, { PureComponent } from 'react';
import { Card, Avatar, Icon } from 'antd';
import { Box } from 'luck-react';
import IconFont from '@/components/IconFont';
import styles from '../index.less';

class ServiceCard extends PureComponent {

  handleDelete = (e)=> {
    e.stopPropagation();
    const { onDelete, dataSource } = this.props;
    if (onDelete) {
      onDelete(dataSource)
    }
  };

  render() {
    const { className, dataSource, state, onClick, onRestart } = this.props;
    const actions = [<div key="update" style={{ fontSize: '12px' }} onClick={()=> {if(onClick) onClick(dataSource)}}>修改</div>];
    if (state) {
      actions.push(<div key="update" style={{ fontSize: '12px' }} onClick={()=> {if(onClick) onRestart(dataSource)}}>重启</div>)
    }
    return (
      <Card
        className={`${styles.card} ${className || ''}`}
        hoverable
        actions={actions}
      >
        <div className={styles.cardBody} onClick={()=> {if(onClick) onClick(dataSource)}}>
          <Box>
            {dataSource.icon ? <IconFont type={state ? dataSource.icon.match(/(\S*)-/)[1] : dataSource.icon} style={{fontSize:'32px'}} /> : (
              <Avatar className={`${styles.icon} ${state ? styles.activeIcon : ''}`}>
                {dataSource.label.substr(0, 1)}
              </Avatar>
            )}
            <Box width="calc(100% - 48px)" height={48} align="center">
              <p className={`${styles.name} ${state ? styles.activeName : ''}`}>{dataSource.label}</p>
            </Box>
          </Box>
          <p style={{marginLeft: 8, color: '#999'}}>{dataSource.name || ''}</p>
        </div>
        {dataSource.type === 'other' && (
          <span className={styles.close}>
            <span wanted="delete" key="delete" onClick={this.handleDelete}>
              <Icon type="close" />
            </span>
          </span>
        )}
      </Card>
    );
  }
}

export default ServiceCard;