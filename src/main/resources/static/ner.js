//Main.js

var main = {
	getNerData : function(cb) {
	$.ajax({
		url: "/ner/get",
		type: "GET",
		success: function(response){
			var globalData = {};
			var items = response.data.data;
			// add questions
			for(var j= items.length;j--;){
				globalData[items[j].title] = {'question':items[j].value}
			}
			// add ner
			var $table = $('#data'),
				$nerTable = $('#nerResult'),
				items = globalData,
				firstHeader = 'Title',
				secondHeader = 'Question',
				tableHTML = '<table dir="ltr" width="800" border="1" class="tablesorter" id="myTable" summary="purpose/structure for speech output">'+
                        //"<caption>This table lets us see the data in a tabular format.</caption>"+
                        '<colgroup width="20%" />'+
                        '<colgroup id="colgroup" class="colgroup" align="center" valign="middle" title="title" width="1*" span="3" style="background:#ddd;" />'+
                        '<thead> <tr>  <th>'+firstHeader+'</th> <th>'+secondHeader+'</th></tr> </thead><tbody>';
	        for(var key in globalData){
	            var title = key;
	            var value = globalData[key].question;
	            tableHTML += '<tr><td>'+ title+'</td><td>'+ value +'</td></tr>'
        	}

        	tableHTML += '</tbody></table>';
        	$table.append(tableHTML);

        	// add ners
        	var nerResultHtml = '<table dir="ltr" width="800" border="1" class="tablesorter" id="myTable2" summary="purpose/structure for speech output">'+
                        //"<caption>This table lets us see the data in a tabular format.</caption>"+
                        '<colgroup width="20%" />'+
                        '<colgroup id="colgroup" class="colgroup" align="center" valign="middle" title="title" width="1*" span="3" style="background:#ddd;" />'+
                        '<thead> <tr>  <th>'+"NER"+'</th> <th>'+"Frequency"+'</th></tr> </thead><tbody>',
                ners = JSON.parse(response.ner).NamedEntities;

                if(ners.length){
	                for(var i=ners.length;i--;){
	                	nerResultHtml += '<tr><td>'+ ners[i].word+'</td><td>'+ ners[i].freq +'</td></tr>';
	                }
            	}
        	nerResultHtml += '</tbody></table>';
        	$nerTable.append(nerResultHtml);
        	$('#category').html("<p>Choosen Category: <br /><br /> <b>"+response.data.category + '</b></p>');
        	main.removeProgressBar();
        	cb();
		},
		error: function(response){
			$('#data').text(response);
			$( "#progressbar" ).progressbar("destroy");
			$("#loading").text('Data failed to load');
		}
	})
},
	addTableSorter : function(){
		$('#myTable2').tablesorter();
		$('#myTable').tablesorter();
	},
	setProgressBar : function(){
		// A little UI magic
		var $bar = $('#progressbar');
		$bar.progressbar({value:0});
		$bar.height(30);
		$bar.width(100);
		$('#loading').text('Loading...');
		setTimeout(updateProgress, 500);
	},
	removeProgressBar : function(){
		clearTimeout(updateProgress);
		$( "#progressbar" ).progressbar("destroy");
		$("#loading").text('');
	}
};

var updateProgress = function(){
	 var progress;
	  progress = $("#progressbar").progressbar("option","value");

	  if (progress < 100) {
	      $("#progressbar").progressbar("option", "value", progress + 5);
	      setTimeout(this.updateProgress, 500);
	  }
	  else if(progress >= 100){
	  		$("#progressbar").progressbar("option", "value", 0);
	  		setTimeout(this.updateProgress, 500);
		}
}

$(document).ready(function() {
	main.setProgressBar();
	main.getNerData(main.addTableSorter);
});
