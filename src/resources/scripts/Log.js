var levelArray = ["error", "warning", "info", "success"];
/**
 * This function creates a new message div with given message and given log level
 * 
 * @param {type} msg
 * @param {type} level
 * @returns {undefined}
 */
function addMessage(msg, level) {
	if ($.trim(level).length <= 0) {
		level = "success";
	}
	level = level.toLowerCase();
	if (level === "severe") {
		level = "error";
	}
	if (levelArray.indexOf(level) < 0) {
		level = "success";
	}
	$(".messages").prepend(
			["<div class='alert alert-", level.toLocaleLowerCase(), "'>", msg, "</div>"].join(""));
	if (!$("#btn" + level).hasClass("active")) {
		$(".messages > .alert:first").hide();
	}
}

/**
 * This method will toggle messages based on the button that is clicked.
 * If the button has active class then show messages otherwise hide them
 * @param {type} btnId
 * @returns {undefined}
 */
function toggleMessages(btnId) {
	var level = btnId.substring(3);
	if ($("#" + btnId).hasClass("active")) {
		$(".messages > .alert-" + level).show();
	} else {
		$(".messages > .alert-" + level).hide();
	}
}

/**
 * This method clears all the log messages
 * @returns {undefined}
 */
function clearLogs() {
	$(".messages").empty();
}

/**
 * Setup event handler code.
 * @param {type} $
 * @returns {undefined}
 */
jQuery(function($) {
	$().UItoTop({easingType: 'easeOutQuart'});
	$(".btn-group > .btn").on("click", function() {
		//This trick is to make sure the active flag is reflected correctly
		setTimeout(toggleMessages, 500, $(this).attr("id"));
	});

	$("#btnClear").on("click", function() {
		clearLogs();
	});
});

