describe('PRECONDITIONS', {
	'Ext should exist': function() {
		value_of(Ext).should_not_be_undefined();
		value_of(Ext).should_not_be(null);
	},

	'USGS Utils should exist': function(){
		value_of(USGS).should_not_be_undefined();
		value_of(USGS).should_not_be(null);
	},

	'SESSION should exist': function() {
		value_of(SESSION).should_not_be_undefined();
		value_of(SESSION).should_not_be(null);
	}

});

describe('Session', {
	'before': function() {

	},

	'getData() has three pieces': function(){
		var data = SESSION.getData();
		value_of(data).should_include("PredictionContext");
		value_of(data).should_include("PermanentMapState");
		value_of(data).should_include("TransientMapState");
	},

	'*getPredictionContext() returns the prediction context': function(){
		value_of(SESSION.getPredictionContext()).should_be(SESSION.getData().PredictionContext);
	},

	'asSessionXML() should contain getPredictionContextAsXML()': function(){
		var sessionXML = SESSION.asSessionXML();
		var predConXML = SESSION.getPredictionContextAsXML();
		value_of(sessionXML).should_have_at_least(10, "characters");
		value_of(predConXML).should_have_at_least(10, "characters");
		var notFound = -1;
		value_of(sessionXML.indexOf(predConXML)).should_not_be(notFound);
	},

	'TODO test changed()': function(){
	},

	'asJSON() returns valid JSON object according to Ext': function(){
		var decoded = Ext.decode(SESSION.asJSON()); // no exceptions should be thrown
		value_of(decoded).should_not_be(null);

	},

	'test mark()': function(){
		//value_of(SESSION.prevState).should_be(null);
		SESSION.mark();
		value_of(SESSION.prevState).should_not_be(null);
		value_of(Ext.decode(SESSION.prevState)).should_not_be(null); // valid json object
	},

	'TODO test isChangedFrom()': function(){
	},

	'TODO test getBinning()': function(){
	}



});

describe('GroupBehaviorsMixin', {
    before: function() {
    },

    after: function() {
    },

	'PRECONDITION: SESSION should have no groups': function() {
		value_of(SESSION.getAllGroups()).should_have(0, "items");
	},

    '*addGroup() should increase groups by 1': function() {
        var group = SESSION.addGroup("dummy", "", "", "");
        value_of(group["@name"]).should_be("dummy");
        value_of(SESSION.getAllGroups()).should_have(1, "items");
    },

	'*removeGroup() should decrease groups by 1': function() {
		var groups = SESSION.removeGroup("dummy");
        value_of(groups[0]["@name"]).should_be("dummy");
		value_of(SESSION.getAllGroups()).should_have(0, "items");
	},

    'removeGroup() should return null for non-existent group': function() {
        var group = SESSION.addGroup("dummy", "", "", "");
        var groups = SESSION.removeGroup("asdf");
        value_of(groups).should_be_null();
	},

    '*getGroup() should get appropriate group': function() {
        var group = SESSION.getGroup("dummy");
        value_of(group["@name"]).should_be("dummy");
	},

    'getGroup() should return null for non-existent group': function() {
        var group = SESSION.getGroup("asdf");
        value_of(group).should_be_null();
	},

    '*groupExists(): non-existent group should not exist ': function() {
        value_of(SESSION.groupExists("asdf")).should_be_false();
	},

    '*groupExists(): existent group should exist': function() {
        value_of(SESSION.groupExists("dummy")).should_be_true();
	},

    '*getAllGroups() should return two groups': function() {
		SESSION.addGroup("dummyToo", "", "", "");
        value_of(SESSION.getAllGroups()).should_have(2, "items");
	},

    '*getAllGroupNames() should return two names': function() {
        value_of(SESSION.getAllGroupNames()).should_have(2, "items");
        value_of(SESSION.getAllGroupNames()[0]).should_be("dummy");
	}

});

describe('ReachBehaviorsMixin', {
    before: function() {
    },

    after: function() {
    },

    '*addReachToGroup() increases reaches by 1': function() {
        var reach = SESSION.addReachToGroup("dummy", "001", "reach001");
        value_of(reach["@name"]).should_be("reach001");

        var group = SESSION.getGroup("dummy");
        value_of(group["reach"]).should_have(1, "items");
    },

    '*isReachMemberOf() should return false for non-member reach': function() {
        value_of(SESSION.isReachMemberOf("002", "dummy")).should_be_false();
    },

    '*isReachMemberOf() should be true for member reach': function() {
        value_of(SESSION.isReachMemberOf("001", "dummy")).should_be_true();
    },

    'getGroupNamesFor() should be empty for non-existent reach': function() {
        value_of(SESSION.getGroupNamesFor("002")).should_be_empty();
    },

    'getGroupNamesFor() should return at least one group for member reach': function() {
        var groupNames = SESSION.getGroupNamesFor("001");
        value_of(groupNames).should_have(1, "items");
        value_of(groupNames[0]).should_be("dummy");
    },

    'removeReachFromGroup() removing non-existent reach should leave groups as is': function() {
    	SESSION.removeReachFromGroup("002", "dummy");

        var group = SESSION.getGroup("dummy");
        value_of(group.reach).should_have(1, "items");
    },

    '*removeReachFromGroup() decreases reaches by 1': function() {
    	SESSION.removeReachFromGroup("dummy", "001");

        var group = SESSION.getGroup("dummy");
        value_of(group.reach).should_have(0, "items");
    },

    '*addLogicalSetToGroup() increases number of sets by 1': function() {
        var set = SESSION.addLogicalSetToGroup("dummy", "set", "001", "set001");
        value_of(set.criteria["@name"]).should_be("set001");

        var group = SESSION.getGroup("dummy");
        value_of(group.logicalSet).should_have(1, "items");
    },

    '*isLogicalSetMemberOf() returns false for non-member set': function() {
        value_of(SESSION.isLogicalSetMemberOf("dummy", "set", "002")).should_be_false();
    },

    '*isLogicalSetMemberOf() returns true for member set': function() {
        value_of(SESSION.isLogicalSetMemberOf("dummy", "set", "001")).should_be_true();
    },

    'removeLogicalSetFromGroup() non-existent set should leave groups as is': function() {
    	SESSION.removeLogicalSetFromGroup("dummy", "set", "002");

        var group = SESSION.getGroup("dummy");
        value_of(group.logicalSet).should_have(1, "items");
    },

    '*removeLogicalSetFromGroup() decreases sets by 1': function() {
    	SESSION.removeLogicalSetFromGroup("dummy", "set", "001");

        var group = SESSION.getGroup("dummy");
        value_of(group.logicalSet).should_have(0, "items");
    }

});

describe('ReachAdjustmentBehaviorsMixin', {
    before: function() {
    },

    after: function() {
    },


    'getAllAdjustedReaches() should be empty': function() {
        value_of(SESSION.getAllAdjustedReaches()).should_be_empty();
    },

    'getAdjustedReach() should be null when no adjustments exist': function() {
        value_of(SESSION.getAdjustedReach("001")).should_be_null();
    },

    'addAdjustment() to unadjusted reach should add adjustment': function() {
    	SESSION.addAdjustment("001", "reach001", "0", "1000");

        var reach = SESSION.getAdjustedReach("001");
        value_of(reach.adjustment).should_have(1, "items");
        value_of(reach.adjustment[0]["@abs"]).should_be("1000");
    },

    'addAdjustment() to already adjusted reach should add adjustment': function() {
    	SESSION.addAdjustment("001", "reach001", "1", "50");

        var reach = SESSION.getAdjustedReach("001");
        value_of(reach.adjustment).should_have(2, "items");
        value_of(reach.adjustment[1]["@abs"]).should_be("50");
    },

    'modify adjustment for source value on reach': function() {
    	SESSION.addAdjustment("001", "reach001", "0", "200");

        var reach = SESSION.getAdjustedReach("001");
        value_of(reach.adjustment[0]["@abs"]).should_be("200");
    },

    'removeAdjustment() on non-existent reach should leave as is': function() {
    	SESSION.removeAdjustment("002", "0");

        var reach = SESSION.getAdjustedReach("001");
        value_of(reach.adjustment).should_have(2, "items");
    },

    'removeAdjustment() on non-existent adjustment should leave as is': function() {
    	SESSION.removeAdjustment("001", "2");

        var reach = SESSION.getAdjustedReach("001");
        value_of(reach.adjustment).should_have(2, "items");
    },

    '*removeAdjustment() should decrease adjustments by 1': function() {
    	SESSION.removeAdjustment("001", "0");

        var reach = SESSION.getAdjustedReach("001");
        value_of(reach.adjustment).should_have(1, "items");
    },

    'removeAdjustment() removing last adjustment should remove reach': function() {
    	SESSION.removeAdjustment("001", "1");
        value_of(SESSION.getAdjustedReach("001")).should_be_null();
        value_of(SESSION.getAllAdjustedReaches()).should_be_empty();
    },

    '*removeAdjustedReach() should remove reach': function() {
    	SESSION.addAdjustment("001", "reach001", "0", "1000");
        value_of(SESSION.getAdjustedReach("001")).should_not_be_null();

        SESSION.removeAdjustedReach("001");
        value_of(SESSION.getAdjustedReach("001")).should_be_null();
        value_of(SESSION.getAllAdjustedReaches()).should_be_empty();
    }

});


describe('TargetdReachBehaviorsMixin', {
    before: function() {
    },

    after: function() {
    },

    'isReachTarget() should be false with no targets': function() {
        value_of(SESSION.isReachTarget("001")).should_be_false();
    },

    'addToTargetReaches)( should increase terminal reaches by 1': function() {
    	SESSION.addToTargetReaches("001", "reach001");

        var targets = SESSION.PredictionContext.terminalReaches.reach;
        value_of(targets).should_have(1, "items");
    },

    'isReachTarget() should be true': function() {
        value_of(SESSION.isReachTarget("001")).should_be_true();
    },

    'addToTargetReaches() on already target should leave as is': function() {
    	SESSION.addToTargetReaches("001", "reach001");

        var targets = SESSION.PredictionContext.terminalReaches.reach;
        value_of(targets).should_have(1, "items");
    },

    'removeReachFromTargets() on non-targeted reach should leave as is': function() {
    	SESSION.removeReachFromTargets("002");

        var targets = SESSION.PredictionContext.terminalReaches.reach;
        value_of(targets).should_have(1, "items");
    },

    'removeReachFromTargets() should decrease terminal reaches by 1': function() {
    	SESSION.removeReachFromTargets("001");

        var targets = SESSION.PredictionContext.terminalReaches.reach;
        value_of(targets).should_have(0, "items");
    }

});