import React,{ PureComponent } from 'react';

class PopWindowDetail extends PureComponent{
  static displayName = 'CustomForm';

  buildForm = (form)=> {
    const { formRef, ...rest} = this.props;
    return React.cloneElement(
      form,
      {
        ref: formRef,
        ...rest,
        ...form.props,
      },
      form.props.children
    )
  }
}

export default PopWindowDetail;