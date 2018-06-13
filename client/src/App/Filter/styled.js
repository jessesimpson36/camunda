import styled from 'styled-components';
import {HEADER_HEIGHT} from './../Header/styled';

export const Filter = styled.div`
  display: flex;
  flex-direction: row;
  height: calc(100vh - ${HEADER_HEIGHT}px);
`;

export const Left = styled.div`
  width: 320px;
`;

export const Right = styled.div`
  width: 100%;
  display: flex;
  flex-direction: column;
`;
