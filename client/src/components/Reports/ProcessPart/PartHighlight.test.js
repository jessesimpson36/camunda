import fs from 'fs';

import React from 'react';
import Viewer from 'bpmn-js/dist/bpmn-viewer.production.min';

import PartHighlight from './PartHighlight';

import {mount} from 'enzyme';

console.error = jest.fn();

const xml = fs.readFileSync(__dirname + '/PartHighlight.test.xml', {encoding: 'utf-8'});

const loadXml = async xml =>
  new Promise(resolve => {
    const viewer = new Viewer();
    viewer.importXML(xml, () => resolve(viewer));
  });

it('should correctly calculate flow nodes between two selected nodes', async () => {
  const viewer = await loadXml(xml);
  const registry = viewer.get('elementRegistry');
  const canvas = viewer.get('canvas');

  const start = registry.get('StartEvent_2').businessObject;
  const end = registry.get('EndEvent_2_1').businessObject;

  mount(<PartHighlight nodes={[start, end]} viewer={viewer} />);

  expect(canvas.hasMarker('StartEvent_2', 'PartHighlight')).toBe(true);
  expect(canvas.hasMarker('SubProcess_2', 'PartHighlight')).toBe(true);
  expect(canvas.hasMarker('Gateway_2', 'PartHighlight')).toBe(true);
  expect(canvas.hasMarker('EndEvent_2_1', 'PartHighlight')).toBe(true);
  expect(canvas.hasMarker('SubProcess_4', 'PartHighlight')).toBe(false);
});

it('should work across subprocess boundaries', async () => {
  const viewer = await loadXml(xml);
  const registry = viewer.get('elementRegistry');
  const canvas = viewer.get('canvas');

  const start = registry.get('Task_3').businessObject;
  const end = registry.get('Task_4').businessObject;

  mount(<PartHighlight nodes={[start, end]} viewer={viewer} />);

  expect(canvas.hasMarker('EndEvent_3', 'PartHighlight')).toBe(true);
  expect(canvas.hasMarker('StartEvent_4', 'PartHighlight')).toBe(true);
  expect(canvas.hasMarker('Gateway_2', 'PartHighlight')).toBe(true);
  expect(canvas.hasMarker('StartEvent_3', 'PartHighlight')).toBe(false);
});

it('should work with boundary events', async () => {
  const viewer = await loadXml(xml);
  const registry = viewer.get('elementRegistry');
  const canvas = viewer.get('canvas');

  const start = registry.get('Task_3').businessObject;
  const end = registry.get('Task_5').businessObject;

  mount(<PartHighlight nodes={[start, end]} viewer={viewer} />);

  expect(canvas.hasMarker('BoundaryEvent', 'PartHighlight')).toBe(true);
});
