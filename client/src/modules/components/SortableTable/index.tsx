/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */

import React, {useEffect, useRef} from 'react';
import {observer} from 'mobx-react';
import Table from 'modules/components/Table';
import {InfiniteScroller} from 'modules/components/InfiniteScroller';

import {ColumnHeader} from './ColumnHeader';
import {Skeleton} from './Skeleton';
import {Row} from './Row';

import {
  List,
  ScrollableContent,
  TH,
  THead,
  TRHeader,
  Spinner,
  SkeletonCheckboxBlock,
  Checkbox,
} from './styled';

type HeaderColumn = {
  content: string | React.ReactNode;
  sortKey?: string;
  isDisabled?: boolean;
  isDefault?: boolean;
};

type RowProps = Omit<React.ComponentProps<typeof Row>, 'isSelected'> & {
  checkIsSelected?: () => boolean; //must be a function because it depends on a store update: https://mobx.js.org/react-optimizations.html#function-props-
};

type Props = {
  state: 'skeleton' | 'loading' | 'error' | 'empty' | 'content';
  headerColumns: HeaderColumn[];
  rows: RowProps[];
  skeletonColumns: React.ComponentProps<typeof Skeleton>['columns'];
  emptyMessage?: string;
  isSelectable?: boolean;
  checkIsAllSelected?: () => boolean; //must be a function because it depends on a store update: https://mobx.js.org/react-optimizations.html#function-props-
  onSelectAll?: () => void;
} & Pick<
  React.ComponentProps<typeof InfiniteScroller>,
  'onVerticalScrollStartReach' | 'onVerticalScrollEndReach'
>;

const SortableTable: React.FC<Props> = observer(
  ({
    state,
    headerColumns,
    rows,
    skeletonColumns,
    isSelectable = false,
    emptyMessage,
    checkIsAllSelected,
    onSelectAll,
    onVerticalScrollStartReach,
    onVerticalScrollEndReach,
  }) => {
    let scrollableContentRef = useRef<HTMLDivElement | null>(null);

    useEffect(() => {
      if (state === 'loading') {
        scrollableContentRef?.current?.scrollTo?.(0, 0);
      }
    }, [state]);

    return (
      <List>
        <ScrollableContent
          overflow={state === 'skeleton' ? 'hidden' : 'auto'}
          ref={scrollableContentRef}
        >
          {state === 'loading' && <Spinner data-testid="instances-loader" />}
          <Table>
            <THead>
              <TRHeader>
                {headerColumns.map((header, index) => {
                  const {content, sortKey, isDisabled, isDefault} = header;

                  return (
                    <TH key={index}>
                      <>
                        {index === 0 &&
                          isSelectable &&
                          (state === 'skeleton' ? (
                            <SkeletonCheckboxBlock />
                          ) : (
                            <Checkbox
                              title="Select all instances"
                              checked={checkIsAllSelected?.()}
                              onCmInput={onSelectAll}
                              disabled={state !== 'content'}
                            />
                          ))}

                        <ColumnHeader
                          label={content}
                          sortKey={sortKey}
                          isDefault={isDefault}
                          disabled={state !== 'content' || isDisabled}
                        />
                      </>
                    </TH>
                  );
                })}
              </TRHeader>
            </THead>
            {state === 'skeleton' && <Skeleton columns={skeletonColumns} />}
            {state === 'empty' && <div>empty message</div>}
            {state === 'error' && <div>error message</div>}

            <InfiniteScroller
              onVerticalScrollStartReach={onVerticalScrollStartReach}
              onVerticalScrollEndReach={onVerticalScrollEndReach}
              scrollableContainerRef={scrollableContentRef}
            >
              <tbody data-testid="data-list">
                {rows.map(
                  ({id, ariaLabel, content, checkIsSelected, onSelect}) => {
                    const isSelected = checkIsSelected?.();

                    return (
                      <Row
                        key={id}
                        id={id}
                        ariaLabel={ariaLabel}
                        content={content}
                        isSelectable={isSelectable}
                        onSelect={onSelect}
                        isSelected={isSelected}
                      />
                    );
                  }
                )}
              </tbody>
            </InfiniteScroller>
          </Table>
        </ScrollableContent>
      </List>
    );
  }
);
export {SortableTable};
