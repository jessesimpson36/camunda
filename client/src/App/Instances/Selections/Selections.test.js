/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */

import React from 'react';
import {mount} from 'enzyme';

import {ThemeProvider} from 'modules/theme';
import {DataManagerProvider} from 'modules/DataManager';
import {SelectionProvider} from 'modules/contexts/SelectionContext';
import {CollapsablePanelProvider} from 'modules/contexts/CollapsablePanelContext';
import CollapsablePanel from 'modules/components/CollapsablePanel';
import ComboBadge from 'modules/components/ComboBadge';

import {FILTER_SELECTION} from 'modules/constants';
import {formatGroupedWorkflows} from 'modules/utils/instance';
import {groupedWorkflowsMock} from 'modules/testUtils';

import Selections from './Selections';

import {countStore} from './Selections.setup';

import * as Styled from './styled';

jest.mock('modules/utils/bpmn');

const renderNode = props =>
  mount(
    <DataManagerProvider>
      <ThemeProvider>
        <CollapsablePanelProvider>
          <SelectionProvider
            groupedWorkflows={formatGroupedWorkflows(groupedWorkflowsMock)}
            filter={FILTER_SELECTION.incidents}
          >
            <Selections countStore={props} />
          </SelectionProvider>
        </CollapsablePanelProvider>
      </ThemeProvider>
    </DataManagerProvider>
  );

describe('Selections', () => {
  it('should render with skelton badge', () => {
    // given
    const node = renderNode(countStore);

    // then

    const header = node.find(CollapsablePanel.Header);
    expect(header.contains('Selections')).toBe(true);

    // badge
    const leftBadge = header
      .find(ComboBadge.Left)
      .find('div[data-test="badge"]');
    const rightBadge = header
      .find(ComboBadge.Right)
      .find('div[data-test="badge"]');
    expect(leftBadge.text()).toEqual('');
    expect(rightBadge.text()).toEqual('');

    // SelectionList
    const body = node.find(CollapsablePanel.Body);
    expect(body.find('SelectionList')).toHaveLength(1);
  });
});
