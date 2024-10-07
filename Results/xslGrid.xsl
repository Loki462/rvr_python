<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:template match="/">
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
<style type="text/css">
* {
  box-sizing: border-box;
}

.row::after {
  content: "";
  clear: both;
  display: table;
}

[class*="col-"] {
  float: left;
  padding: 15px;
}

html {
  font-family: "Lucida Sans", sans-serif;
}

.header {
  color: #ffffff;
  padding: 10px;
  background-color: #1227b1;
  align: center;
  }
table {
	border: 1px solid black;
	max-width: 100% ;
}

td {
	border: 1px solid black;
	background-color: white;
	color: black;
	text-align:center
}

th {
	border: 1px solid black;
	background-color: #2E9AFE;
	color: white;
	text-align:middle;
}
.item1 { grid-area: header; }
.item2 { grid-area: menu; }
.item3 { grid-area: main; }
.item4 { grid-area: right; }
.item5 { grid-area: footer; }

.grid-container {
  display: grid;
  grid-template-areas:
    'menu header header header header'
    'menu main main main main'
    'footer footer footer footer footer';
  grid-gap: 10px;
  background-color: #2196F3;
  padding: 10px;
  align: center;
}

.grid-container > div {
  background-color: rgba(255, 255, 255, 0.8);
  text-align: center;
  padding: 0px;
  font-size: 30px;
  
}
.collapsible {
  background-color: #777;
  color: white;
  cursor: pointer;
  padding: 18px;
  width: 100%;
  border: none;
  text-align: left;
  outline: none;
  font-size: 15px;
}

.active, .collapsible:hover {
  background-color: #555;
}

.collapsible:after {
  content: '\002B';
  color: white;
  font-weight: bold;
  float: right;
  margin-left: 5px;
}

.active:after {
  content: "\2212";
}

.content {
  padding: 0 18px;
  max-height: 0;
  overflow: hidden;
  transition: max-height 0.2s ease-out;
}
</style>
<script>
var coll = document.getElementsByClassName("collapsible");
var length = coll.length;
var i;

for (i = 0; i &lt; length; i++){
	coll[i].addEventListener("click", function() {
	this.classList.toggle("active");
    var content=this.nextElementSibling;
    if (content.style.maxHeight) {
      content.style.maxHeight = null;
    } else {
      content.style.maxHeight = content.scrollHeight + "px";
    } 
  });
}
</script>
</head>

<body>
<header class="header">
	<xsl:for-each select="root/ExecutionDateList/ExecutionDateInfo">
	  <h1 style="text-align: center">Salesforce Quality Analyzer Tool</h1>
	  <h2 style="text-align: center">Completed Salesforce Org comparison for dates between <xsl:value-of select="CurrentConfigDate" /> and <xsl:value-of select="lastConfigRunDate"/> .</h2>
	</xsl:for-each>
</header>
<div class="grid-container">
  <div class="item1">
	<table style="width:100%;align:center">
		<tr class='heading'> 
			 <th colspan="5" style="margin-top: 1px;margin-bottom: 1px;font-size:15px">High Level Org and Logged-in User details</th> 
		</tr>
		<tr>
			<th style="width:400px">User Name:</th>
			<th style="width:250px">Email ID:</th>
			<th style="width:250px">Profile Name:</th>
			<th style="width:200px">Sandbox:</th>
			<th style="width:200px">API Version:</th>
		</tr>
		<xsl:for-each select="root/HighLevelLoginInfoList/HighLevelLoginInfo">
			<tr>
				<td class="colfmt">
					<xsl:value-of select="userName" />
				</td>
				<td class="colfmt">
					<xsl:value-of select="emailID" />
				</td>
				<td class="colfmt">
					<xsl:value-of select="profileName" />
				</td>
				<td class="colfmt">
					<xsl:value-of select="sandbox" />
				</td>
				<td class="colfmt">
					<xsl:value-of select="APIVersion" />
				</td>
			</tr>
		</xsl:for-each>
	</table>
	<table style="width:100%;align:center">
		<tr>
			<th style="width:400px">Org Name:</th>
			<th style="width:250px">Org ID:</th>
			<th style="width:250px">Org Instance:</th>
			<th style="width:200px">Org Edition:</th>
			<th style="width:200px">Org Primary Contact:</th>
		</tr>
		<xsl:for-each select="root/HighLevelOrgInfoList/HighLevelOrgInfo">
			<tr>
				<td class="colfmt">
					<xsl:value-of select="OrgName" />
				</td>
				<td class="colfmt">
					<xsl:value-of select="OrgID" />
				</td>
				<td class="colfmt">
					<xsl:value-of select="OrgInstance" />
				</td>
				<td class="colfmt">
					<xsl:value-of select="OrgEdition" />
				</td>
				<td class="colfmt">
					<xsl:value-of select="OrgPrimaryContact" />
				</td>
			</tr>
		</xsl:for-each>
	</table>
  </div>
  <div class="item2">
  <table class="tfmt3" style="width:100%;height:100%;align:center">
  		<tr class='heading'> 
			 <th colspan="2" style="margin-top: 1px;margin-bottom: 1px;font-size:15px">High Level Component wise Comparison Result</th> 
		</tr>
		<tr>
			<th style="width:350px">Component Name:</th>
			<th style="width:250px">Total:</th>
		</tr>
		<xsl:for-each select="root/HighLevelComparisonList/HighLevelComparison">
			<tr>
				<td class="colfmt">
					<xsl:value-of select="ComponentName" />
				</td>
				<td class="colfmt">
					<xsl:value-of select="ComponentCount" />
				</td>
			</tr>
		</xsl:for-each>
	</table>
  </div>
  <div class="item3" style="width:100%;height:100%;align:center">
  <table class="content" style="width:100%;height:100%;align:center">
		<tr>
			<th style="width:250px">Objects Impacted due to Last Deployment:</th>
		</tr>
		<xsl:for-each select="root/ObjectDetailsList/ObjectDetailInfo">
			<tr>
				<td class="colfmt">
					<xsl:value-of select="ObjectNames" />
				</td>
			</tr>
		</xsl:for-each>
	</table>
  </div>
  <div class="item5" id="collapsible" style="width:100%;height:100%;align:center">
  <table class="content" style="width:100%;height:100%;align:center">
		<tr class='heading'> 
			 <th colspan="5" style="margin-top: 1px;margin-bottom: 1px;font-size:15px">Detail Report of Org Comparison</th> 
		</tr>
		<tr>
			<th style="width:250px">Metadata Type:</th>
			<th style="width:250px">Metadata Name:</th>
			<th style="width:250px">Metadata Id:</th>
			<th style="width:350px">Created Date:</th>
			<th style="width:350px">Last Modified Date:</th>
		</tr>
		<xsl:for-each select="root/DetailComparisonList/DetailComparisonInfo">
			<tr>
				<td class="colfmt">
					<xsl:value-of select="metadataType" />
				</td>
				<td class="colfmt">
					<xsl:value-of select="metadataName" />
				</td>
				<td class="colfmt">
					<xsl:value-of select="metadataID" />
				</td>
				<td class="colfmt">
					<xsl:value-of select="createdDate" />
				</td>
				<td class="colfmt">
					<xsl:value-of select="lastModifieDate" />
				</td>
			</tr>
		</xsl:for-each>
	</table>
  </div>
</div>

</body>
</html>
</xsl:template>
</xsl:stylesheet>