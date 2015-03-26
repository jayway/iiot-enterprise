'use strict';
var aws = require('aws-sdk');
var async = require('async');
var influx = require('influx');

function sendToCloudWatch(items, callback) {
    var metrics = items.map(function(data) {
        console.log('tag', data.tag);
        console.log('rest', data.timestamp, data.metric, data.installationId);
        return {
            MetricName: data.tag,
            Dimensions: [
                {
                    Name: 'installationId',
                    Value: data.installationId
                }
            ],
            Timestamp: data.timestamp,
            Unit: 'None',
            Value: data.metric
        };
    });
    var params = {
        MetricData: metrics,
        Namespace: 'Team Enterprise'
    };
    var cloudwatch = new aws.CloudWatch();
    cloudwatch.putMetricData(params, function(err, data) {
        if (err) console.log(err, err.stack);
        else     console.log(data);
        return callback(err, data);
    });
}


function sendToInflux(items, callback) {
    var dbInflux = influx({
        host : 'sandbox.influxdb.com',
        port : 8086,
        username : 'jlowgren',
        password : 'u#icel4G$1IwM53i',
        database : 'iiot_enterprise_db'
    });
    var series =  {
        metrics: items
    };
    var options = {}
    console.log('Sending to influx', series, options);
    dbInflux.writeSeries(series, options, callback);
}

function processMetrics(data, callback) {
    async.parallel([
        sendToCloudWatch.bind(null, data),
        sendToInflux.bind(null, data)
    ], callback);
}

processMetrics.handler = function(event, context) {
    console.log(JSON.stringify(event, null, '  '));
    var items = [];
    for(var i = 0; i < event.Records.length; ++i) {
        var encodedPayload = event.Records[i].kinesis.data;
        var payload = new Buffer(encodedPayload, 'base64').toString('utf8');
        console.log("Decoded payload: " + payload);
        var item = JSON.parse(payload);
        if (item.tag && item.metric)
            items.push(item);
    }
    processMetrics(items, function(err, result) {
        context.done(err, JSON.stringify(result, null, 2));
    });
};

module.exports = processMetrics;


if(require.main === module) {
    console.log("called directly");
    var data =[{
        "timestamp": "2015-03-24T18:25:43.511Z",
        "installationId": "123123q",
        "tag": "temp",
        "metric": "22.1"
    }];
    sendToInflux(data, function(err, res) {
        console.log("Influx", err, res);
    });
}

