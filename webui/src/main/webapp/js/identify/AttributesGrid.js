/*
 * Grid component representing the Attributes tab in the Reach Identify popup
 * window.  This grid displays attribute values specific to the reach being
 * identified.
 */
AttributesGrid = Ext.extend(Ext.grid.GridPanel, {
    // Default grid properties
    title: 'Reach/Catchment Info',
    store: null,
    viewConfig: {autoFill: true, forceFit: true},
    autoScroll: false,
    enableColumnHide: false,
    enableColumnMove: false,
    enableHdMenu: false,
    hideHeaders: true,
    trackMouseOver: false,
    disableSelection: true,
    region: 'center',
    
    // Custom properties for this extension
    reachResponse: null,
    
    // Initialization of the grid - set up the data store and columns
    initComponent: function() {
        
        // The data store for our data model - attribute name and value pairs
        var attributeReader = new Ext.data.ArrayReader({}, [
            {name: 'section'},
            {name: 'name'},
            {name: 'value'}
        ]);
        // Use a grouping store to group the rows by section
        this.store = new Ext.data.GroupingStore({
            reader: attributeReader,
            data: [],
            sortInfo: {field: 'name', direction: 'ASC'},
            groupField: 'section'
        });
        this.loadStore();
    
        // Define the column model and apply it to the grid
        Ext.apply(this, {
            columns: [
                {header: 'Section', dataIndex: 'section', hidden: true},
                {header: 'Name', dataIndex: 'name'},
                {header: 'Value', dataIndex: 'value', renderer: this.renderNumbersWithColumns}
            ],
            view: new Ext.grid.GroupingView({
                forceFit: true,
                startCollapsed: true,
                groupTextTpl: '{group}'
            })
        });

        this.on('viewready', function(grid) {
        	grid.view.toggleGroup(grid.view.getGroupId('Basic Attributes'), true);
        });
        
        // Call the superclass' init function
        AttributesGrid.superclass.initComponent.apply(this, arguments);
    },
    
    renderNumbersWithColumns : function(value, metaData, record, rowIndex, colIndex, store) {
    	var fieldsToFormat = {
    		"reach length": true,
    		"mean flow":true,
    		"mean velocity":true,
    		"incremental area":true,
    		"cumulative drainage area":true
    	};
    	var formattedVal = value;
    	if(fieldsToFormat[record.data.name.toLowerCase()]) {
    		var parts = value.split(' ');
    		formattedVal = Ext.util.Format.number(parts[0], '0,000.00');
    		if(parts.length>1) formattedVal += ' ' + parts[1];
    	}
    	return Sparrow.USGS.prettyPrintUnitsForHtml(formattedVal);
    }, 
    
    onRender: function() {
    	// Call the superclass' onRender function
        AttributesGrid.superclass.onRender.apply(this, arguments);
    },
    
    /*
     * Customized loader for the values on the Attributes tab
     */
    loadStore: function() {
        this.store.removeAll();
        
        // Set up a record definition for pushing into the store
        var AttributeRecord = Ext.data.Record.create([
            {name: 'section'}, // basic or sparrow
            {name: 'name'},
            {name: 'value'}
        ]);

        var mappedval = this.reachResponse["sparrow-id-response"].results.result[0]['mapped-value'];
        var html = 'Current Mapped Value: ' + Math.round(mappedval.value*1000)/1000 + ' ' + Sparrow.USGS.prettyPrintUnitsForHtml(mappedval.units) + 
    		' of ' + mappedval.constituent + ' (' + mappedval.name + ')';
        document.getElementById('sparrow-identify-mapped-value-a').innerHTML = html;
        document.getElementById('sparrow-identify-mapped-value-b').innerHTML = html;
        var sectionList = this.reachResponse["sparrow-id-response"].results.result[0].attributes.data.section;
        for (var i = 0; i < sectionList.length; i++) {
            var sectionTitle = sectionList[i]["@display"];
            
            var rowList = sectionList[i]["r"];
            for (var j = 0; j < rowList.length; j++) {
                
                var units = rowList[j]["c"][2] ? ' ' + rowList[j]["c"][2] : '';
                var name = rowList[j]["c"][0];
                
                //exclude some values from the list
                if(name.toLowerCase() == "incremental delivery fraction") continue;
                
                // Build a record (row) for the grid and add to the store
                var record = new AttributeRecord({
                    section: sectionTitle,
                    name: name,
                    value: rowList[j]["c"][1] + units
                });
                this.store.add(record);
            }
        }
    },
    
    /*
     * Refreshes the attribute list using the specified reach identify response
     */
    refresh: function(reachResponse) {
        this.reachResponse = reachResponse;
        this.loadStore();
    }
});

Ext.reg('attributesGrid', AttributesGrid);
