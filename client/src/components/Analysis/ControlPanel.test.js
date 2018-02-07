import React from 'react';
import {mount} from 'enzyme';

import ControlPanel from './ControlPanel';

jest.mock('./service', () => {return {
  loadProcessDefinitions: () => [{id:'procdef1'}, {id:'procdef2'}]
}});
jest.mock('../Reports', () => {return {
  Filter: () => 'Filter'
}});

jest.mock('components', () => {
  const Select = props => <select {...props}>{props.children}</select>;
  Select.Option = props => <option {...props}>{props.children}</option>;

  return {
    Select,
    ActionItem: props => <button {...props}>{props.children}</button>,
    Popover: ({children}) => children
  };
});

const data = {
  processDefinitionId: '',
  filter: null
};

const spy = jest.fn();

it('should display available process definitions', async () => {
  const node = mount(<ControlPanel {...data} onChange={spy} />);

  await node.instance().loadAvailableDefinitions();

  expect(node).toIncludeText('procdef1');
  expect(node).toIncludeText('procdef2');
});

it('should contain a gateway and end Event field', () => {
  const node = mount(<ControlPanel {...data} onChange={spy} />);

  expect(node.find('[name="ControlPanel__gateway"]')).toBePresent();
  expect(node.find('[name="ControlPanel__endEvent"]')).toBePresent();
});

it('should show a please select message if an entity is not selected', () => {
  const node = mount(<ControlPanel {...data} onChange={spy} />);

  expect(node).toIncludeText('Please Select End Event');
  expect(node).toIncludeText('Please Select Gateway');
});

it('should show the element name if an element is selected', () => {
  const node = mount(<ControlPanel {...data} onChange={spy} gateway={{
    name: 'I am a Gateway',
    id: 'gatewayId'
  }} />);

  expect(node).toIncludeText('I am a Gateway');
  expect(node).not.toIncludeText('gatewayId');
});

it('should show the element id if an element has no name', () => {
  const node = mount(<ControlPanel {...data} onChange={spy} gateway={{
    name: undefined,
    id: 'gatewayId'
  }} />);

  expect(node).toIncludeText('gatewayId');

})
