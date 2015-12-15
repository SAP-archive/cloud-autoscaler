$(document).ready(init);

var infoPanel = new InfoPanel("alert_http_content");
var infoPanel2 = new InfoPanel("info_http_content");
var actions = new Actions();
var objects;

function init() {
	objects = new Objects();
	objects.initButtons();
	objects.initFields();
}

function Objects() {
	var _accountNameField = document.querySelector("#accountName");
	var _appNameField = document.querySelector("#applicationName");
	
	var initialState = {
			b_GET_APP_INFO : "inline-block",
			b_START_MONITOR : "none",
			b_STOP_MONITOR : "none",
			b_START_APP : "none",
			b_STOP_APP : "none"
	}
	
	function initButtons() {
		initButtonsActions();
		initButtonsDisplay();
	}
	function initButtonsActions() {
		$("button").each(function(index, button) {
			if (typeof ButtonActions[button.id]) {
				$(button).on("click", ButtonActions[button.id]);
			}
		});
	}
	function initButtonsDisplay() {
		$("button").each(function(index, button) {
			if (typeof ButtonActions[button.id]) {
				if (button.id) {
					button.style.display = initialState[button.id];
				}
			}
		});
	}
	
	function initFields() {
		$(_accountNameField).on("keyup", initButtonsDisplay);
		$(_appNameField).on("keyup", initButtonsDisplay);
	}
	
	function hideItem(domObj) {
		domObj.style.display = "none";
	}
	
	function showItem(domObj) {
		domObj.style.display = "inline-block";
	}
	
	function setFieldsReadOnly() {
		_accountNameField.disabled = true;
		_appNameField.disabled = true;
	}
	
	function setFieldsEditable() {
		_accountNameField.disabled = false;
		_appNameField.disabled = false;
	}
	
	return {
		initButtons : initButtons,
		initFields : initFields,
		hideItem : hideItem,
		showItem : showItem,
		setFieldsReadOnly : setFieldsReadOnly,
		setFieldsEditable : setFieldsEditable,
		_accountNameField : _accountNameField,
		_appNameField : _appNameField
	}
}

var ButtonActions = {
	b_GET_APP_INFO : function() {
		var that = this;
		$.ajax({
			url: '/appscaler/backend',
			data: { 
					action : "query", 
					accountName : objects._accountNameField.value, 
					applicationName : objects._appNameField.value 
				   },
			type: 'GET'
		})
		.done(function(data) {
			infoPanel.write(data);
			objects.hideItem(that);
			objects.showItem(document.querySelector("#b_START_MONITOR"));
			objects.showItem(document.querySelector("#b_START_APP"));
			objects.showItem(document.querySelector("#b_STOP_APP"));
		})
		.fail(function(err) {
			console.error(err);
		});
	},
	b_START_MONITOR : function() {
		var that = this;
		$.ajax({
			url: '/appscaler/backend',
			data: {action : "start"},
			type: 'GET'
		})
		.done(function(data) {
			infoPanel2.clear();
			infoPanel2.writeln("Started monitoring.");
			infoPanel2.writeln(data);
			actions.startMonitorDataPolling();
			objects.setFieldsReadOnly();
			objects.hideItem(that);
			objects.showItem(document.querySelector("#b_STOP_MONITOR"));
		})
		.fail(function(err) {
			console.error(err);
		});
	},
	b_STOP_MONITOR : function() {
		var that = this;
		$.ajax({
			url: '/appscaler/backend',
			data: {action : "stop"},
			type: 'GET'
		})
		.done(function(data) {
			infoPanel2.clear();
			infoPanel2.writeln("Stopped monitoring.");
			infoPanel2.writeln(data);
			actions.stopMonitorDataPolling();
			objects.setFieldsEditable();
			objects.hideItem(that);
			objects.showItem(document.querySelector("#b_START_MONITOR"));
		})
		.fail(function(err) {
			console.error(err);
		});
	},
	b_START_APP : function() {
		$.ajax({
			url: '/appscaler/backend',
			data: {action : "startApp"},
			type: 'GET'
		})
		.done(function(data) {
			infoPanel.clear();
			infoPanel.writeln(data);
		})
		.fail(function(err) {
			console.error(err);
		});
	},
	b_STOP_APP : function() {
		$.ajax({
			url: '/appscaler/backend',
			data: {action : "stopApp"},
			type: 'GET'
		})
		.done(function(data) {
			infoPanel.clear();
			infoPanel.writeln(data);
		})
		.fail(function(err) {
			console.error(err);
		});
	}
}

function InfoPanel(id) {
	var domDiv = document.querySelector('#' + id);
	
	function write(data) {
		domDiv.innerHTML = data;
	}

	function append(data) {
		domDiv.innerHTML = domDiv.innerHTML + data;
	}

	function clear() {
		domDiv.innerHTML = "";
	}

	function writeln(data) {
		append(data + "<br>");
	}

	return {
		write : write,
		append : append,
		clear : clear,
		writeln : writeln
	}
}

function Actions() {
	var intervalId;
	function startMonitorDataPolling() {
		intervalId = setInterval(getMonitorData, 20000);
	}

	function stopMonitorDataPolling() {
		clearInterval(intervalId);
	}

	function getMonitorData() {
		$.ajax({
			url: '/appscaler/monitor',
			type: 'GET',
		})
		.done(function(data) {
			infoPanel2.write((new Date).toISOString() + " : <br>" + data);
		})
		.fail(function(err) {
			console.error(err);
		});
	}


	return {
		startMonitorDataPolling : startMonitorDataPolling,
		stopMonitorDataPolling : stopMonitorDataPolling
	}
}

