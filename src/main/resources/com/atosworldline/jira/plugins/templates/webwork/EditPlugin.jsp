<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>Test validation AJAX</title>
		<script type="text/javascript">
			var requete;
			
			function valider() {
			   var donnees = confproject.options[confproject.selectedIndex].value;
			   callServlet("projectId", donnees);
// 			   var url = "/jira/plugins/servlet/editplugin?projectId=" + escape(donnees.value);
// 			   if (window.XMLHttpRequest) {
// 			       requete = new XMLHttpRequest();
// 			   } else if (window.ActiveXObject) {
// 			       requete = new ActiveXObject("Microsoft.XMLHTTP");
// 			   }
// 			   requete.open("GET", url, true);
			   requete.onreadystatechange = majIHM;
			   requete.send(null);
			}
			
			function majIHM() {
				var message = "";				
				if (requete.readyState == 4) {
					if (requete.status == 200) {
		    			var conffield = document.getElementById("conf-field");
		    			conffield.options.length = 1;
		
					    var messageTag = requete.responseXML.getElementsByTagName("messages")[0].getElementsByTagName("message");
					    for (i = 0; i < messageTag.length; i++) {
					    	message = messageTag[i].childNodes[0].nodeValue;
					    	conffield.options[conffield.options.length] = new Option(message, message);
					    }
					}
				}
			}
			  
			  
			function loadProjects() {
				callServlet("", "");		
				requete.onreadystatechange = loadHtmlProjects;
				requete.send(null);
			}
			
			function loadHtmlProjects() {				
				if (requete.readyState == 4) {
					if (requete.status == 200) {
						var confproject = document.getElementById("conf-project");
						confproject.options.length = 1;
		
					    var messageTag = requete.responseXML.getElementsByTagName("projects")[0].getElementsByTagName("project");
					    for (i = 0; i < messageTag.length; i++) {
					    	projectId = messageTag[i].getElementsByTagName("id")[0].childNodes[0].nodeValue;
					    	projectName = messageTag[i].getElementsByTagName("name")[0].childNodes[0].nodeValue;
					    	confproject.options[confproject.options.length] = new Option(projectName, projectId);
					    }
					}
				}
			}
			  
			function callServlet(variable, donnees) {
				if (variable != "") {
					var url = "/jira/plugins/servlet/editplugin?" + variable + "=" + escape(donnees);
				} else {
					var url = "/jira/plugins/servlet/editplugin";
				}
				if (window.XMLHttpRequest) {
					requete = new XMLHttpRequest();
				} else if (window.ActiveXObject) {
				    requete = new ActiveXObject("Microsoft.XMLHTTP");
				}
				requete.open("GET", url, true);
			}
		</script>
	</head>
	<body onload="loadProjects()">
		<table>
			<tr>
				<td>
					Projet :
				</td>
				<td nowrap>
					<select name="conf-project" id="conf-project" onchange="valider()">
						<option value="0">Selectionnez un projet</option>
					</select>
				</td>
			</tr>
			<tr>
				<td>Champ :</td>
				<td nowrap>
					<select name="conf-field" id="conf-field"></select>
				</td>
			</tr>
			<tr>
				<td>Action :</td>
				<td nowrap>
					<select name="conf-actiontodo" id="conf-actiontodo"></select>
				</td>
			</tr>
		</table>
	</body>
</html>