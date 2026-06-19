'use client'

import React, { useState } from 'react';
import useChartButtonStore from '../../../store/chartButtonStore';
import { ChartMinuteEnum } from '../../../util/function/ChartTimeEnum';

export default function ResolutionDropdown() {
  const [focusedNum, setFocusedNum] = useState(null);
  
  const isResolutionOpen = useChartButtonStore((state) => state.isResolutionOpen);
  const selectedResolution = useChartButtonStore((state) => state.selectedResolution);
  const setResolution = useChartButtonStore((state) => state.setResolution);

  if (!isResolutionOpen) return null;

  return (
    <div
      data-radix-popper-content-wrapper=""
      dir="ltr"
      style={{
        position: 'fixed',
        left: '0px',
        top: '0px',
        transform: 'translate(49px, 250px)',
        minWidth: 'max-content',
        '--radix-popper-transform-origin': '0% 0%',
        zIndex: 'auto',
        '--radix-popper-available-width': '1880px',
        '--radix-popper-available-height': '688.21875px',
        '--radix-popper-anchor-width': '57.390625px',
        '--radix-popper-anchor-height': '32px'
      }}
    >
      <div
        data-side="bottom"
        data-align="start"
        role="menu"
        aria-orientation="vertical"
        data-state="open"
        data-radix-menu-content=""
        dir="ltr"
        id="radix-:r_mf:"
        aria-labelledby="radix-:r_me:"
        data-tossinvest-log="DropdownMenu.Content"
        data-contents-label-code="topAffix"
        data-content-tag="DropdownContent"
        className="tw69-dsueog8 tw69-gduhvu0"
        data-overlay-type="Dropdown"
        data-section-name="Dropdown_Resolutions"
        data-overlay-name="Resolutions"
        tabIndex={-1}
        data-orientation="vertical"
        style={{
          outline: 'none',
          '--radix-dropdown-menu-content-transform-origin': 'var(--radix-popper-transform-origin)',
          '--radix-dropdown-menu-content-available-width': 'var(--radix-popper-available-width)',
          '--radix-dropdown-menu-content-available-height': 'var(--radix-popper-available-height)',
          '--radix-dropdown-menu-trigger-width': 'var(--radix-popper-anchor-width)',
          '--radix-dropdown-menu-trigger-height': 'var(--radix-popper-anchor-height)',
          pointerEvents: 'auto'
        }}
      >
        <div className="tw69-dsueogc tw69-dsueoga">
          <div
            dir="ltr"
            style={{
              position: 'relative',
              '--radix-scroll-area-corner-width': '0px',
              '--radix-scroll-area-corner-height': '0px',
              overflow: 'hidden',
              height: 'inherit'
            }}
          >
            <style></style>
            <div
              data-radix-scroll-area-viewport=""
              className="tw69-74lq312"
              style={{ overflow: 'hidden scroll' }}
            >
              <div style={{ minWidth: '100%', display: 'table' }}>
                {Object.values(ChartMinuteEnum).map((num) => (
                  <div
                    key={num}
                    role="menuitem"
                    className="tw69-gduhvu0 tw69-dsueogj tw69-dsueogh tw69-dsueoge"
                    data-tds-wts-dropdown-item=""
                    data-tossinvest-log="Dropdown.Item"
                    data-contents-label="분"
                    data-contents-label-code="분"
                    data-content-value={`${num}분`}
                    data-parent-name="Resolutions"
                    data-component-id="b7giraxr07ok"
                    id={`radix-:r_${num}:`}
                    tabIndex={-1}
                    data-orientation="vertical"
                    data-radix-collection-item=""

                    onMouseEnter={() => setFocusedNum(num)}
                    onMouseLeave={() => setFocusedNum(null)}
                    onClick={() => setResolution(num)}
                    data-tds-wts-dropdown-item-focus={
                      focusedNum === num || selectedResolution === num ? "" : undefined
                    }
                  >
                    <span className="tw69-dsueogv">
                      {num}
                      {"분"}
                    </span>
                  </div>
                ))}

              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}