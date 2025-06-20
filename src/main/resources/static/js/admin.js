$(document).ready(function () {
            if (typeof interact !== 'function') {
                $('#generateLayoutBtn').prop('disabled', true).css('opacity', 0.5);
                return;
            }
            const seatRowsContainer = $('#seat-rows-container');
            const seatLayoutPreviewTarget = $('#seat-rows-preview-target');
            const noLayoutMessage = $('#seatLayoutPreview .no-layout-message');
            const screenPreviewDiv = $('#seatLayoutPreview .screen-preview');
            const seatAssignmentSection = $('#seatAssignmentSection');
            const finalSaveBtn = $('#finalSaveBtn');
            const generateLayoutBtn = $('#generateLayoutBtn');
            const addRowBtn = $('#add-seat-row-definition-btn');
            const capacityDisplayInput = $('#capacityDisplay');
            const seatAssignmentsHiddenInput = $('#seatAssignmentsField');
            const roomForm = $('#roomForm');
            const globalErrorDiv = $('#roomForm .global-error');

            const initialRowDefinitionsFromServer = /*[[${roomForm?.rowDefinitions != null ? roomForm.rowDefinitions : #lists.newList()}]]*/ [];
            const initialSeatAssignmentsJson = /*[[${roomForm?.seatAssignmentsJson != null ? roomForm.seatAssignmentsJson : '[]'}]]*/ '[]';
            const initialAvailableSeatTypesRaw = /*[[${availableSeatTypes}]]*/ null;
            const availableSeatTypes = initialAvailableSeatTypesRaw === null ? [] : initialAvailableSeatTypesRaw;

            let nextRowIndex = initialRowDefinitionsFromServer.length;
            let seatAssignments = {};
            let currentGeneratedRowDefinitions = [];

            function isColorDark(hexColor) {
                if (!hexColor || typeof hexColor !== 'string') return true; 
                let color = hexColor.startsWith('#') ? hexColor.substring(1) : hexColor;

                if (color.length === 3) {
                    color = color[0] + color[0] + color[1] + color[1] + color[2] + color[2];
                }

                if (color.length !== 6) {
                    return true;
                }

                const r = parseInt(color.substring(0, 2), 16);
                const g = parseInt(color.substring(2, 4), 16);
                const b = parseInt(color.substring(4, 6), 16);
                const luma = (0.2126 * r + 0.7152 * g + 0.0722 * b);
                return luma < 128;
            }

            $('.draggable-seat-type').each(function() {
                const bgColor = $(this).data('seat-type-color') || $(this).css('background-color');
                if (isColorDark(bgColor)) {
                    $(this).css('color', 'white');
                } else {
                    $(this).css('color', 'black');
                }
            });

            function calculateTempCapacityFromInputs() {
                let tempCap = 0;
                seatRowsContainer.find('.seats-per-row-input').each(function () {
                    const seats = parseInt($(this).val());
                    if (!isNaN(seats) && seats > 0) tempCap += seats;
                });
                $('#capacityDisplay').val(tempCap);
            }

            function calculateCapacityFromGeneratedLayout() {
                let tempCap = 0;
                if (Array.isArray(currentGeneratedRowDefinitions)) {
                    currentGeneratedRowDefinitions.forEach(def => { if (def && !isNaN(def.numberOfSeats)) tempCap += parseInt(def.numberOfSeats); });
                }
                $('#capacityDisplay').val(tempCap);
            }

            function createRowDefinitionHTML(index, identifier = '', seats = 10, order = 0, existingRowDefId = '', seatTypeIdForRow = null) {
                const seatTypeIdValue = (seatTypeIdForRow !== null && seatTypeIdForRow !== undefined && String(seatTypeIdForRow).toLowerCase() !== "null" && String(seatTypeIdForRow) !== "") ? String(seatTypeIdForRow) : '';
                let seatTypeOptionsHTML = '<option value="">-- System Default --</option>';

                if (Array.isArray(availableSeatTypes) && availableSeatTypes.length > 0) {
                    availableSeatTypes.forEach(type => {
                        if (type && typeof type.id !== 'undefined' && type.id !== null &&
                            typeof type.name === 'string' && type.name.trim() !== '') {

                            const typeIdForOption = String(type.id);
                            const selected = (typeIdForOption === seatTypeIdValue && typeIdValue !== '') ? 'selected' : '';
                            seatTypeOptionsHTML += `<option value="${typeIdForOption}" ${selected}>${type.name}</option>`;
                        }
                    });
                } 
                return `
                <div class="row-definition" data-row-index="${index}">
                    <input type="hidden" name="rowDefinitions[${index}].id" value="${existingRowDefId || ''}">
                    <div class="row-identifier-input-container">
                        <label for="rowDefinitions${index}Identifier" class="form-label">Row ID <span class="required-star">*</span></label>
                        <input type="text" id="rowDefinitions${index}Identifier" name="rowDefinitions[${index}].identifier" class="form-control form-control-sm row-identifier-input" value="${identifier}" placeholder="e.g., A" required style="text-transform: uppercase;"/>
                        <div class="text-danger small field-error"></div>
                    </div>
                    <div class="seats-per-row-input-container">
                        <label for="rowDefinitions${index}NumberOfSeats" class="form-label"># Seats <span class="required-star">*</span></label>
                        <input type="number" id="rowDefinitions${index}NumberOfSeats" name="rowDefinitions[${index}].numberOfSeats" class="form-control form-control-sm seats-per-row-input" value="${seats}" placeholder="# Seats" min="1" max="15" required/>
                        <div class="text-danger small field-error"></div>
                    </div>
                    <div class="row-order-input-container">
                        <label for="rowDefinitions${index}Order" class="form-label">Order <span class="required-star">*</span></label>
                        <input type="number" id="rowDefinitions${index}Order" name="rowDefinitions[${index}].order" class="form-control form-control-sm row-order-input" value="${order}" placeholder="Order" min="0" required/>
                        <div class="text-danger small field-error"></div>
                    </div>
                    <div class="row-default-seattype-container">
                        <label for="rowDefinitions${index}SeatTypeId" class="form-label">Default Type</label>
                        <select id="rowDefinitions${index}SeatTypeId" name="rowDefinitions[${index}].seatTypeId" class="form-select form-select-sm row-default-seat-type-select">
                            ${seatTypeOptionsHTML}
                        </select>
                    </div>
                    <div class="remove-btn-container"> <i class="fas fa-times btn-remove-row" title="Remove this row"></i> </div>
                </div>`;
            }
             addRowBtn.on('click', function () {
                    let maxOrder = -1;
                    seatRowsContainer.find('.row-order-input').each(function() {
                        const val = parseInt($(this).val());
                        if (!isNaN(val) && val > maxOrder) maxOrder = val;
                    });
                    const newOrder = seatRowsContainer.find('.row-definition').length === 0 ? 0 : maxOrder + 1;
                    const validNextRowIndex = Number.isFinite(nextRowIndex) ? nextRowIndex : seatRowsContainer.find('.row-definition').length;
                    const defaultIdentifier = String.fromCharCode(65 + validNextRowIndex);
                    const newRowHTML = createRowDefinitionHTML(validNextRowIndex, defaultIdentifier, 10, newOrder, '', null);
                    seatRowsContainer.append(newRowHTML);
                    nextRowIndex = validNextRowIndex + 1; 
                    calculateTempCapacityFromInputs();
            });

            seatRowsContainer.on('click', '.btn-remove-row', function () {
                if (confirm('Are you sure you want to remove this row definition?')) { 
                    const removedRowIndex = $(this).closest('.row-definition').data('row-index'); 
                    $(this).closest('.row-definition').remove();
                    calculateTempCapacityFromInputs();

                    if (seatAssignmentSection.is(':visible')) {
                        generateLayoutBtn.click();
                    }
                }
            });
            
            function switchToStep1() {
                seatAssignmentSection.slideUp();
                finalSaveBtn.fadeOut().prop('disabled', true);
                seatRowsContainer.find('input:not([type=hidden]), select, textarea').prop('readonly', false);
                seatRowsContainer.find('.btn-remove-row').prop('disabled', false).show();
                addRowBtn.prop('disabled', false).show();
                generateLayoutBtn.text('Generate Layout & Proceed').removeClass('btn-warning btn-danger').addClass('btn-info').prop('disabled', false); 
                calculateTempCapacityFromInputs();
            }

            function switchToStep2() {
                seatAssignmentSection.slideDown(function() { 
                });
                finalSaveBtn.fadeIn().prop('disabled', false);
                seatRowsContainer.find('input:not([type=hidden]), select, textarea').prop('readonly', true);
                seatRowsContainer.find('.btn-remove-row').prop('disabled', true).hide();
                addRowBtn.prop('disabled', true).hide();
                generateLayoutBtn.text('Re-generate Layout (Resets Assignments)').removeClass('btn-info').addClass('btn-warning').prop('disabled', false);
            }

            generateLayoutBtn.on('click', function () {
                if (seatAssignmentSection.is(':visible')) {
                    if (!confirm('Re-generating the layout will reset all current seat assignments and row definitions will be unlocked. Are you sure?')) {
                        return;
                    }
                    currentGeneratedRowDefinitions = []; seatAssignments = {};
                    seatLayoutPreviewTarget.empty();
                    if(noLayoutMessage && noLayoutMessage.length) noLayoutMessage.show();
                    if(screenPreviewDiv && screenPreviewDiv.length) screenPreviewDiv.hide();
                    if(capacityDisplayInput && capacityDisplayInput.length) capacityDisplayInput.val(0);
                    switchToStep1();
                    return;
                }

                currentGeneratedRowDefinitions = []; seatAssignments = {};
                let isValidStructure = true, rowIdentifiersSet = new Set(), hasDefinedRows = false;

                seatRowsContainer.find('.row-definition').each(function () {
                    hasDefinedRows = true;
                    const $rowDef = $(this);
                    const identifierInput = $rowDef.find('.row-identifier-input');
                    const seatsInput = $rowDef.find('.seats-per-row-input');
                    const orderInput = $rowDef.find('.row-order-input');
                    const seatTypeIdDropdown = $rowDef.find('.row-default-seat-type-select');
                    let seatTypeIdFromDTOValue = seatTypeIdDropdown.length ? seatTypeIdDropdown.val() : null;
                    if (seatTypeIdFromDTOValue === "") seatTypeIdFromDTOValue = null;

                    $rowDef.find('.field-error').text(''); $rowDef.css('border', '');
                    const identifier = identifierInput.val().trim().toUpperCase();
                    const numberOfSeats = parseInt(seatsInput.val());
                    const order = parseInt(orderInput.val());
                    const existingRowId = $rowDef.find('input[name$=".id"]').val();
                    let rowIsValid = true;

                    if (!identifier) { rowIsValid = false; isValidStructure = false; identifierInput.closest('.row-identifier-input-container').find('.field-error').text('Row ID is required.'); }
                    else if (rowIdentifiersSet.has(identifier)) { rowIsValid = false; isValidStructure = false; identifierInput.closest('.row-identifier-input-container').find('.field-error').text('Row ID must be unique.'); }
                    else { rowIdentifiersSet.add(identifier); }
                    if (isNaN(numberOfSeats) || numberOfSeats < 1) {
                        rowIsValid = false; isValidStructure = false;
                        seatsInput.closest('.seats-per-row-input-container').find('.field-error').text('Seats must be > 0.');
                    } else if (numberOfSeats > 15) {
                        rowIsValid = false; isValidStructure = false;
                        seatsInput.closest('.seats-per-row-input-container').find('.field-error').text('Max 15 seats per row.');
                    }
                     if (isNaN(order) || order < 0) { rowIsValid = false; isValidStructure = false; orderInput.closest('.row-order-input-container').find('.field-error').text('Order must be >= 0.'); }

                    else {
                        currentGeneratedRowDefinitions.push({
                            id: existingRowId || null, identifier, numberOfSeats, order,
                            seatTypeIdFromDTO: seatTypeIdFromDTOValue
                        });
                    }
                });

                if (!isValidStructure || !hasDefinedRows || (currentGeneratedRowDefinitions.length === 0 && hasDefinedRows)) {
                    const message = !hasDefinedRows ? 'Please define at least one row.' : 'Please correct errors in row definitions.';
                    if (globalErrorDiv.length) globalErrorDiv.html(`<p>${message}</p>`).show();
                    else alert(message);
                    finalSaveBtn.fadeOut().prop('disabled', true); 
                    return;
                }
                if (globalErrorDiv.length) globalErrorDiv.hide();
                currentGeneratedRowDefinitions.sort((a, b) => a.order - b.order);
                renderSeatLayoutPreviewDOM(currentGeneratedRowDefinitions);
                switchToStep2();
            });

            function renderSeatLayoutPreviewDOM(rowDefinitionsData) {
                seatLayoutPreviewTarget.empty();
                seatAssignments = {};
                let hasVisibleRows = false;

                if (screenPreviewDiv && screenPreviewDiv.length) screenPreviewDiv.hide();
                if (noLayoutMessage && noLayoutMessage.length) noLayoutMessage.show();

                if (!Array.isArray(rowDefinitionsData) || rowDefinitionsData.length === 0) {
                } else {
                    rowDefinitionsData.forEach(def => { 
                        if (def.identifier && def.numberOfSeats > 0) {
                            hasVisibleRows = true;
                            const $seatRowPreview = $('<div>').addClass('seat-row-preview');
                            $seatRowPreview.append($('<div>').addClass('row-identifier-preview').text(def.identifier));
                            const $seatCellsContainer = $('<div>').addClass('d-flex flex-wrap justify-content-center');

                            const rowDefaultSeatType = availableSeatTypes.find(st => String(st.id) === String(def.seatTypeIdFromDTO));
                            const isRowDefaultCouple = rowDefaultSeatType ? !!rowDefaultSeatType.isCouple : false;

                            for (let i = 0; i < def.numberOfSeats; i++) { 
                                const seatNumber = i + 1; 
                                const seatKey = `${def.identifier}_${seatNumber}`;

                                let initialTypeId = null;
                                let initialTypeName = 'Unassigned';
                                let initialColor = '#6c757d';
                                let initialIsPartOfCouple = false;

                                if (rowDefaultSeatType) {
                                    if (isRowDefaultCouple) {
                                        if (i % 2 === 0 && (i + 1) < def.numberOfSeats) { 
                                            initialTypeId = rowDefaultSeatType.id;
                                            initialTypeName = rowDefaultSeatType.name;
                                            initialColor = rowDefaultSeatType.color;
                                            initialIsPartOfCouple = true;
                                        } else if (i % 2 !== 0) { 
                                            const prevSeatKey = `${def.identifier}_${seatNumber - 1}`;
                                            if (seatAssignments[prevSeatKey] &&
                                                seatAssignments[prevSeatKey].isPartOfCouple &&
                                                String(seatAssignments[prevSeatKey].typeId) === String(rowDefaultSeatType.id)) {
                                                initialTypeId = rowDefaultSeatType.id;
                                                initialTypeName = rowDefaultSeatType.name;
                                                initialColor = rowDefaultSeatType.color;
                                                initialIsPartOfCouple = true;
                                            }
                                        }
                                    } else { 
                                        initialTypeId = rowDefaultSeatType.id;
                                        initialTypeName = rowDefaultSeatType.name;
                                        initialColor = rowDefaultSeatType.color;
                                        initialIsPartOfCouple = false;
                                    }
                                }

                                seatAssignments[seatKey] = {
                                    rowIdentifier: def.identifier,
                                    seatNumber: seatNumber,
                                    typeId: initialTypeId,
                                    typeName: initialTypeName,
                                    color: initialColor,
                                    isPartOfCouple: initialIsPartOfCouple
                                };

                                const $seatCell = $('<div>')
                                    .addClass('seat-cell-preview dropzone')
                                    .attr('title', `Seat: ${def.identifier}${seatNumber} (${initialTypeName})`)
                                    .text(initialTypeId ? String(initialTypeName).substring(0, 1).toUpperCase() : seatNumber)
                                    .css('background-color', initialColor)
                                    .data({
                                        'seat-key': seatKey,
                                        'seat-identifier': def.identifier,
                                        'seat-number': seatNumber,
                                        'seat-number-display': seatNumber
                                    });

                                if (isColorDark(initialColor)) {
                                    $seatCell.css('color', 'white');
                                } else {
                                    $seatCell.css('color', 'black');
                                }
                                $seatCellsContainer.append($seatCell);
                            }
                            $seatRowPreview.append($seatCellsContainer);
                            seatLayoutPreviewTarget.append($seatRowPreview);
                        }
                    });
                }

                if (hasVisibleRows) {
                    if (noLayoutMessage && noLayoutMessage.length) noLayoutMessage.hide();
                    if (screenPreviewDiv && screenPreviewDiv.length) screenPreviewDiv.show();
                    setTimeout(initializeDropzonesForPreview, 50); 
                } else {
                    if (noLayoutMessage && noLayoutMessage.length) noLayoutMessage.show();
                    if (screenPreviewDiv && screenPreviewDiv.length) screenPreviewDiv.hide();
                }
                calculateCapacityFromGeneratedLayout();
            }

            function initializeDraggableSeatTypes() {
                if (typeof interact === 'undefined') { return; }
                interact('.seat-type-palette-form .draggable-seat-type').draggable({
                    inertia: true,
                    autoScroll: true,
                    listeners: {
                        start(event) { $(event.target).addClass('dragging'); },
                        move: dragMoveListener,
                        end(event) {
                            $(event.target).removeClass('dragging');
                            event.target.style.transform = 'translate(0px, 0px)';
                            event.target.setAttribute('data-x', 0);
                            event.target.setAttribute('data-y', 0);
                        }
                    }
                });
            }

            function dragMoveListener(event) {
                const target = event.target;
                const x = (parseFloat(target.getAttribute('data-x')) || 0) + event.dx;
                const y = (parseFloat(target.getAttribute('data-y')) || 0) + event.dy;
                target.style.transform = `translate(${x}px, ${y}px)`;
                target.setAttribute('data-x', x);
                target.setAttribute('data-y', y);
            }

            function initializeDropzonesForPreview() {
                if (typeof interact !== 'function') { return; }
                interact('.seat-cell-preview.dropzone').dropzone({
                    accept: '.draggable-seat-type',
                    overlap: 'pointer',
                    ondropactivate: function (event) { event.target.classList.add('drop-active'); },
                    ondragenter: function (event) { 
                        const dropzoneSeatDiv = $(event.target);
                        const draggableTypeDiv = $(event.relatedTarget);
                        const draggedColor = draggableTypeDiv.data('seat-type-color') || '#3498db'; 
                        const draggedTypeName = draggableTypeDiv.data('seat-type-name') || '';
                        const draggedIsCouple = !!availableSeatTypes.find(st => String(st.id) === String(draggableTypeDiv.data('seat-type-id')))?.isCouple;
                        dropzoneSeatDiv.addClass('drop-target').css('background-color', draggedColor).text(String(draggedTypeName).substring(0, 1).toUpperCase());
                        if (isColorDark(draggedColor)) { dropzoneSeatDiv.css('color', 'white'); } else { dropzoneSeatDiv.css('color', 'black'); }
                        if (draggedIsCouple) {
                            const targetSeatNumber = parseInt(dropzoneSeatDiv.data('seat-number'));
                            if (targetSeatNumber % 2 !== 0) { 
                                const neighborSeatKey = `${dropzoneSeatDiv.data('seat-identifier')}_${targetSeatNumber + 1}`;
                                const $neighborCell = $(`.seat-cell-preview[data-seat-key="${neighborSeatKey}"]`);
                                if ($neighborCell.length && !$neighborCell.hasClass('occupied-by-another-drag')) { 
                                    $neighborCell.addClass('drop-target-neighbor').css('background-color', draggedColor).text(String(draggedTypeName).substring(0, 1).toUpperCase());
                                    if (isColorDark(draggedColor)) $neighborCell.css('color', 'white'); else $neighborCell.css('color', 'black');
                                }
                            }
                        }
                    },
                    ondragleave: function (event) {
                        const dropzoneSeatDiv = $(event.target);
                        const seatKey = dropzoneSeatDiv.data('seat-key');
                        const currentAssignment = seatAssignments[seatKey]; 
                        dropzoneSeatDiv.removeClass('drop-target');
                        if (currentAssignment) {
                            dropzoneSeatDiv.css('background-color', currentAssignment.color).text(currentAssignment.typeId ? String(currentAssignment.typeName).substring(0, 1).toUpperCase() : dropzoneSeatDiv.data('seat-number-display'));
                            if (isColorDark(currentAssignment.color)) { dropzoneSeatDiv.css('color', 'white'); } else { dropzoneSeatDiv.css('color', 'black'); }
                        }
                        const draggableTypeDiv = $(event.relatedTarget);
                        const draggedIsCouple = !!availableSeatTypes.find(st => String(st.id) === String(draggableTypeDiv.data('seat-type-id')))?.isCouple;
                        if (draggedIsCouple) {
                            const targetSeatNumber = parseInt(dropzoneSeatDiv.data('seat-number'));
                            if (targetSeatNumber % 2 !== 0) {
                                const neighborSeatKey = `${dropzoneSeatDiv.data('seat-identifier')}_${targetSeatNumber + 1}`;
                                const $neighborCell = $(`.seat-cell-preview[data-seat-key="${neighborSeatKey}"]`);
                                const neighborAssignment = seatAssignments[neighborSeatKey];
                                if ($neighborCell.length && neighborAssignment) {
                                    $neighborCell.removeClass('drop-target-neighbor').css('background-color', neighborAssignment.color).text(neighborAssignment.typeId ? String(neighborAssignment.typeName).substring(0, 1).toUpperCase() : $neighborCell.data('seat-number-display'));
                                    if (isColorDark(neighborAssignment.color)) $neighborCell.css('color', 'white'); else $neighborCell.css('color', 'black');
                                }
                            }
                        }
                    },
                    ondrop: function (event) {
                        const dropzoneSeatDiv = $(event.target);
                        const draggableTypeDiv = $(event.relatedTarget);
                        const targetSeatKey = dropzoneSeatDiv.data('seat-key');
                        const targetRowIdentifier = dropzoneSeatDiv.data('seat-identifier');
                        const targetSeatNumber = parseInt(dropzoneSeatDiv.data('seat-number'));
                        let droppedSeatTypeIdRaw = draggableTypeDiv.attr('data-seat-type-id') || draggableTypeDiv.data('seat-type-id');
                        const droppedSeatTypeId = (droppedSeatTypeIdRaw === "" || droppedSeatTypeIdRaw === undefined || String(droppedSeatTypeIdRaw).toLowerCase() === "null") ? null : droppedSeatTypeIdRaw;
                        const droppedSeatTypeObject = availableSeatTypes.find(st => String(st.id) === String(droppedSeatTypeId));
                        const droppedIsCouple = droppedSeatTypeObject ? !!droppedSeatTypeObject.isCouple : false;
                        function updateSingleSeat(seatKeyToUpdate, typeToApply, isPartOfNewCouple = false) {
                            if (!seatAssignments[seatKeyToUpdate]) return;
                            const typeId = typeToApply ? typeToApply.id : null;
                            const typeName = typeToApply ? typeToApply.name : 'Unassigned';
                            const typeColor = typeToApply ? typeToApply.color : '#6c757d';
                            seatAssignments[seatKeyToUpdate].typeId = typeId;
                            seatAssignments[seatKeyToUpdate].typeName = typeName;
                            seatAssignments[seatKeyToUpdate].color = typeColor;
                            seatAssignments[seatKeyToUpdate].isPartOfCouple = isPartOfNewCouple;
                            const $cell = $(`.seat-cell-preview[data-seat-key="${seatKeyToUpdate}"]`);
                            if ($cell.length) {
                                $cell.text(typeId ? String(typeName).substring(0, 1).toUpperCase() : $cell.data('seat-number-display')).css('background-color', typeColor).attr('title', `Seat: ${$cell.data('seat-identifier')}${$cell.data('seat-number-display')} (${typeName})`);
                                if (isColorDark(typeColor)) $cell.css('color', 'white'); else $cell.css('color', 'black');
                            }
                        }
                        if (seatAssignments[targetSeatKey] && seatAssignments[targetSeatKey].isPartOfCouple) {
                            const oldPairPartnerNumber = targetSeatNumber % 2 === 0 ? targetSeatNumber - 1 : targetSeatNumber + 1;
                            const oldPairPartnerKey = `${targetRowIdentifier}_${oldPairPartnerNumber}`;
                            if (seatAssignments[oldPairPartnerKey] && seatAssignments[oldPairPartnerKey].isPartOfCouple) { updateSingleSeat(oldPairPartnerKey, null, false); }
                            updateSingleSeat(targetSeatKey, null, false);
                        }
                        if (droppedIsCouple) {
                            if (targetSeatNumber % 2 !== 0) { 
                                const neighborSeatNumber = targetSeatNumber + 1;
                                const neighborSeatKey = `${targetRowIdentifier}_${neighborSeatNumber}`;
                                if (seatAssignments[neighborSeatKey]) { 
                                    if(seatAssignments[neighborSeatKey].isPartOfCouple){ const neighborOldPairPartnerNumber = neighborSeatNumber % 2 === 0 ? neighborSeatNumber - 1 : neighborSeatNumber + 1; if(neighborOldPairPartnerNumber !== targetSeatNumber){ updateSingleSeat(`${targetRowIdentifier}_${neighborOldPairPartnerNumber}`, null, false); } }
                                    updateSingleSeat(targetSeatKey, droppedSeatTypeObject, true); 
                                    updateSingleSeat(neighborSeatKey, droppedSeatTypeObject, true); 
                                } else { updateSingleSeat(targetSeatKey, null, false); }
                            } else { updateSingleSeat(targetSeatKey, null, false); }
                        } else { updateSingleSeat(targetSeatKey, droppedSeatTypeObject, false); }
                        const finalAssignmentsForSubmit = Object.values(seatAssignments).map(sa => ({ rowIdentifier: sa.rowIdentifier, seatNumber: sa.seatNumber, typeId: (sa.typeId === "" || sa.typeId === undefined || String(sa.typeId).toLowerCase() === "null") ? null : sa.typeId }));
                        seatAssignmentsHiddenInput.val(JSON.stringify(finalAssignmentsForSubmit));
                    }
                });
            }

            function loadExistingDataForEdit() {
                const isEditMode = /*[[${roomForm.id != null && roomForm.id != 0}]]*/ false;
                const hasExistingRowsInDOM = seatRowsContainer.find('.row-definition').length > 0;
                if (isEditMode) {
                    if (hasExistingRowsInDOM && initialSeatAssignmentsJson && initialSeatAssignmentsJson !== "[]") {
                        currentGeneratedRowDefinitions = initialRowDefinitionsFromServer.sort((a,b) => a.order - b.order).map(def => ({ id: def.id, identifier: def.identifier, numberOfSeats: def.numberOfSeats, order: def.order, seatTypeIdFromDTO: (def.seatTypeId === "" || def.seatTypeId === undefined || def.seatTypeId === null) ? null : def.seatTypeId }));
                        renderSeatLayoutPreviewDOM(currentGeneratedRowDefinitions);
                        try {
                            const assignmentsFromServer = JSON.parse(initialSeatAssignmentsJson);
                            assignmentsFromServer.forEach(assignment => {
                                const seatKey = `${assignment.rowIdentifier}_${assignment.seatNumber}`;
                                if (seatAssignments[seatKey]) { 
                                    const typeIdToAssign = (assignment.seatTypeId === "" || assignment.seatTypeId === undefined || assignment.seatTypeId === null) ? null : assignment.seatTypeId;
                                    const seatType = availableSeatTypes.find(st => String(st.id) === String(typeIdToAssign));
                                    seatAssignments[seatKey].typeId = typeIdToAssign;
                                    seatAssignments[seatKey].typeName = seatType ? seatType.name : (typeIdToAssign ? 'Unknown Type' : 'Unassigned');
                                    seatAssignments[seatKey].color = seatType ? (seatType.color || '#6c757d') : '#6c757d';
                                    const $seatCell = $(`.seat-cell-preview[data-seat-key="${seatKey}"]`);
                                    if ($seatCell.length) {
                                        $seatCell.text(typeIdToAssign ? String(seatAssignments[seatKey].typeName).substring(0,1).toUpperCase() : $seatCell.data('seat-number-display')).css('background-color', seatAssignments[seatKey].color);
                                        if (isColorDark(seatAssignments[seatKey].color)) $seatCell.css('color', 'white'); else $seatCell.css('color', 'black');
                                        $seatCell.attr('title', `Seat: ${$seatCell.data('seat-identifier')}${$seatCell.data('seat-number-display')} (${seatAssignments[seatKey].typeName})`);
                                    }
                                }
                            });
                        } catch (e) {switchToStep1(); return;}
                        switchToStep2();
                    } else {
                        if (!hasExistingRowsInDOM) { addRowBtn.click(); }
                        switchToStep1();
                    }
                } else {
                    if (!hasExistingRowsInDOM) { addRowBtn.click(); }
                    switchToStep1(); 
                }
                calculateTempCapacityFromInputs(); 
            }

            initializeDraggableSeatTypes();
            loadExistingDataForEdit();

            $('#roomForm').on('submit', function(e) {
                if (seatAssignmentSection.is(':hidden') || finalSaveBtn.is(':hidden') || finalSaveBtn.prop('disabled')) { e.preventDefault(); return false; }
                const finalAssignments = Object.values(seatAssignments).map(sa => ({ rowIdentifier: sa.rowIdentifier, seatNumber: sa.seatNumber, seatTypeId: (sa.typeId === "" || sa.typeId === undefined) ? null : sa.typeId }));
                $('#seatAssignmentsField').val(JSON.stringify(finalAssignments));
            });
        });