/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a proprietary license.
 * See the License.txt file for more information. You may not use this file
 * except in compliance with the proprietary license.
 */

import {currentTheme} from './currentTheme';

import {getStateLocally, clearStateLocally} from 'modules/utils/localStorage';

describe('stores/currentTheme', () => {
  beforeEach(() => {
    currentTheme.reset();
    clearStateLocally();
  });

  it('should select the system theme by default', () => {
    expect(currentTheme.state.selectedTheme).toBe('system');
  });

  it('should change the selected theme', () => {
    expect(currentTheme.state.selectedTheme).toBe('system');

    currentTheme.changeTheme('dark');

    expect(currentTheme.state.selectedTheme).toBe('dark');
    expect(getStateLocally().theme).toBe('dark');
    expect(currentTheme.theme).toBe('dark');

    currentTheme.changeTheme('light');
    expect(currentTheme.state.selectedTheme).toBe('light');
    expect(getStateLocally().theme).toBe('light');
    expect(currentTheme.theme).toBe('light');

    currentTheme.changeTheme('system');
    expect(currentTheme.state.selectedTheme).toBe('system');
    expect(getStateLocally().theme).toBe('system');
    expect(currentTheme.theme).toBe('light');
  });
});
