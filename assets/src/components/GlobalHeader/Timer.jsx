import React, { PureComponent } from 'react';
import { formatMessage } from 'umi-plugin-react/locale';
import moment from "moment/moment";

class Timer extends PureComponent {
  weekArr = [
    formatMessage({id: 'component.globalHeader.monday'}),
    formatMessage({id: 'component.globalHeader.tuesday'}),
    formatMessage({id: 'component.globalHeader.wednesday'}),
    formatMessage({id: 'component.globalHeader.thursday'}),
    formatMessage({id: 'component.globalHeader.friday'}),
    formatMessage({id: 'component.globalHeader.saturday'}),
    formatMessage({id: 'component.globalHeader.sunday'}),
  ];

  formatTime = ()=> {
    const now = moment();
    const date = now.format('YYYY年MM月DD日');
    const week = this.weekArr[now.format('E') - 1];
    const time = now.format('HH:mm:ss');
    return `${date} ${week} ${time}`;
  };

  constructor(props) {
    super(props);
    this.state = {
      now: this.formatTime(),
    };
    this.timer = setInterval(()=> {
      this.setState({
        now: this.formatTime(),
      })
    }, 1000)
  }

  componentWillUnmount() {
    if (this.timer) clearInterval(this.timer);
  }

  render() {
    const { now } = this.state;
    return now;
  }
}

export default Timer;