Java.perform(function () {
    var ContentProvider = Java.use("android.content.ContentProvider");

    ContentProvider.query.overload(
        'android.net.Uri', '[Ljava.lang.String;', 'android.os.Bundle', 'android.os.CancellationSignal'
    ).implementation = function (uri, projection, queryArgs, cancellationSignal) {
        console.log("Query intercepted: " + uri.toString());

        var result = this.query(uri, projection, queryArgs, cancellationSignal);

        // Log the cursor column names and first few rows for inspection
        if (result != null) {
            var columnNames = result.getColumnNames();
            console.log("Columns: " + columnNames.join(", "));

            while (result.moveToNext()) {
                var row = {};
                for (var i = 0; i < columnNames.length; i++) {
                    row[columnNames[i]] = result.getString(i);
                }
                console.log("Row: " + JSON.stringify(row));
            }
            result.close();
        }

        return result;
    };

    rpc.exports = {
        setauthority: function (authority) {
            console.log("Authority set to: " + authority);
        }
    };
});
