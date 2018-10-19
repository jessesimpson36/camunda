import {parseQueryString, getPayload} from './service';

describe('Instances service', () => {
  describe('parseQueryString', () => {
    it('should return a empty object for invalid querys string', () => {
      const invalidInputA = '?filter={"active":truef,"incidents":true}';
      const invalidInputB = '?filter=';
      const invalidInputC = '';

      expect(parseQueryString(invalidInputA)).toEqual({});
      expect(parseQueryString(invalidInputB)).toEqual({});
      expect(parseQueryString(invalidInputC)).toEqual({});
    });

    it('should return an object for valid query strings', () => {
      const input =
        '?filter={"a":true,"b":true,"c":"X", "array": ["lorem", "ipsum"]}';
      const output = {
        filter: {a: true, b: true, c: 'X', array: ['lorem', 'ipsum']}
      };

      expect(parseQueryString(input)).toEqual(output);
    });

    it('should support query strings with more params', () => {
      const input = '?filter={"a":true,"b":true,"c":"X"}&extra={"extra": true}';
      const output = {
        filter: {a: true, b: true, c: 'X'},
        extra: {extra: true}
      };

      expect(parseQueryString(input)).toEqual(output);
    });
  });
});

describe('Selection services', () => {
  let state;

  it('should return payload containing included ids', () => {
    //when
    state = {
      selection: {all: false, ids: ['foo'], excludeIds: []},
      selections: [],
      filter: {incidents: true}
    };

    //then
    expect(getPayload({state})).toMatchSnapshot();
  });

  it('should return payload containing empty excluded ids', () => {
    //when
    state = {
      selection: {all: true, ids: [], excludeIds: []},
      selections: [],
      filter: {incidents: true}
    };

    //then
    expect(getPayload({state})).toMatchSnapshot();
  });

  it('should return payload containing excluded ids', () => {
    //when
    state = {
      selection: {all: true, ids: [], excludeIds: ['foo']},
      selections: [],
      filter: {incidents: true}
    };

    //then
    expect(getPayload({state})).toMatchSnapshot();
  });
});
