import React from 'react';
import classnames from 'classnames';
import {Input, Dropdown} from 'components';

import './Typeahead.css';

const valuesShownInBox = 10;
const valueHeight = 30;

export default class Typeahead extends React.Component {
  state = {
    query: '',
    optionsVisible: false,
    selectedValueIdx: 0,
    firstShownOptionIdx: 0
  };

  componentDidMount() {
    document.body.addEventListener('click', this.close);
  }

  componentDidUpdate(prevProps) {
    if (this.props.initialValue && this.props.initialValue !== prevProps.initialValue) {
      this.setState({
        query: this.getFormatter()(this.props.initialValue)
      });
    }
  }

  componentWillUnmount() {
    document.body.removeEventListener('click', this.close);
  }

  returnFocusToInput = evt => {
    if (evt.target === this.optionsList) {
      this.input.focus();
    }
  };

  showOptions = evt => {
    if (evt && evt.type === 'click') {
      this.input.select();
    }

    if (!this.state.optionsVisible) {
      this.setState({
        optionsVisible: true
      });
    }
  };

  close = evt => {
    if (!evt || !this.container.contains(evt.target)) {
      this.setState({
        optionsVisible: false
      });
    }
  };

  updateValues = () => {
    this.setState({
      selectedValueIdx: 0,
      firstShownOptionIdx: 0
    });
  };

  updateQuery = async evt => {
    this.setState({query: evt.target.value});
    this.showOptions();
    this.updateValues();
  };

  getFormatter = () => this.props.formatter || (v => v);

  selectValue = value => () => {
    const formatter = this.getFormatter();
    this.setState({
      query: formatter(value),
      selectedValueIdx: 0,
      firstShownOptionIdx: 0,
      optionsVisible: false
    });

    if (this.props.onSelect) {
      this.props.onSelect(value);
    }
    this.close();
  };

  handleKeyPress = evt => {
    evt = evt || window.event;

    if (evt.key === 'Tab') {
      this.close();
      return;
    }

    const {selectedValueIdx, optionsVisible} = this.state;
    const values = this.getFilteredValues();
    if (evt.key === 'Enter') {
      evt.preventDefault();
      if (optionsVisible && values[selectedValueIdx]) {
        this.selectValue(values[selectedValueIdx])();
      }
      return;
    }

    if (evt.key === 'Escape') {
      if (this.state.optionsVisible) {
        evt.stopPropagation();
      }
      this.close();
    } else {
      let selectedValueIdx = this.state.selectedValueIdx;

      if (evt.key === 'ArrowDown') {
        evt.preventDefault();
        if (!this.state.optionsVisible) {
          this.showOptions();
        } else {
          selectedValueIdx = (selectedValueIdx + 1) % values.length;
        }
      }

      if (evt.key === 'ArrowUp') {
        evt.preventDefault();
        selectedValueIdx = selectedValueIdx - 1 < 0 ? values.length - 1 : selectedValueIdx - 1;
      }

      const firstShownOptionIdx = this.scrollIntoView(selectedValueIdx);

      this.setState({
        selectedValueIdx,
        firstShownOptionIdx
      });
    }
  };

  scrollIntoView = selectedValueIdx => {
    let {firstShownOptionIdx} = this.state;
    const values = this.getFilteredValues();

    if (this.optionsList) {
      if (selectedValueIdx === 0) {
        this.optionsList.scrollTop = 0;
        firstShownOptionIdx = 0;
      }

      if (selectedValueIdx === values.length - 1) {
        this.optionsList.scrollTop = this.optionsList.scrollHeight;
        firstShownOptionIdx = values.length - valuesShownInBox;
      }

      if (selectedValueIdx >= firstShownOptionIdx + valuesShownInBox) {
        this.optionsList.scrollTop = (firstShownOptionIdx + 1) * valueHeight;
        firstShownOptionIdx++;
      }

      if (selectedValueIdx < firstShownOptionIdx) {
        firstShownOptionIdx--;
        this.optionsList.scrollTop = selectedValueIdx * valueHeight;
      }
    }

    return firstShownOptionIdx;
  };

  optionsListRef = optionsList => {
    this.optionsList = optionsList;
    if (optionsList) {
      this.optionsList.highestWidth = this.optionsList.offsetWidth;
    }
  };

  containerRef = container => {
    this.container = container;
  };

  inputRef = input => {
    this.input = input;
  };

  getFilteredValues = () =>
    this.props.values.filter(value =>
      this.getFormatter()(value)
        .toLowerCase()
        .includes(this.state.query.toLowerCase())
    );

  render() {
    const formatter = this.getFormatter();

    const {query, selectedValueIdx, optionsVisible} = this.state;
    const values = this.getFilteredValues();

    const searchResultContainerStyle = {
      maxHeight: valueHeight * valuesShownInBox + 'px',
      maxWidth: this.optionsList && this.optionsList.highestWidth + 'px'
    };

    const valueStyle = {
      height: valueHeight + 'px'
    };

    return (
      <div ref={this.containerRef} className="Typeahead">
        <Input
          className="Typeahead__input"
          value={query}
          onChange={this.updateQuery}
          onClick={this.showOptions}
          onKeyDown={this.handleKeyPress}
          ref={this.inputRef}
        />
        {optionsVisible &&
          values.length > 0 && (
            <div
              style={searchResultContainerStyle}
              className="Typeahead__search-result-list"
              ref={this.optionsListRef}
              onMouseUp={this.returnFocusToInput}
            >
              {values.map((value, index) => {
                return (
                  <Dropdown.Option
                    className={classnames('Typeahead__search-result', {
                      'is-active': index === selectedValueIdx
                    })}
                    style={valueStyle}
                    onClick={this.selectValue(value)}
                    key={formatter(value)}
                  >
                    {formatter(value)}
                  </Dropdown.Option>
                );
              })}
            </div>
          )}
      </div>
    );
  }
}
