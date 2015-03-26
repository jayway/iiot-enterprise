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
    console.log('Sending to cloudwatch!');
    var cloudwatch = new aws.CloudWatch();
    cloudwatch.putMetricData(params, function(err, data) {
       console.log('cloudwatch response!');
        if (err) console.log(err, err.stack);
        else     console.log(data);
        return callback(err, data);
    });
}


function sendToInflux(items, callback) {
    var dbInflux = influx({
        host : 'sandbox.influxdb.com',
        port : 8086,
        username : 'enterprise',
        password : 'x3li01-XWs[e3WAp',
        database : 'team_enterprise_db'
    });
    console.log('Preparing data for influx');
    var series = items.reduce(function(result, current) {
      var series = "lambda."+current.installationId;
      console.log('adding to', series);
      if (!(series in result)) {
        result[series] = [];
      }
      result[series].push(current);
      return result;
    }, {}); 
    console.log('Sending to influx', series);
    var options = {}
    dbInflux.writeSeries(series, options, function(err, data) {
       console.log('influx response!');
        if (err) console.log(err, err.stack);
        else     console.log(data);
        return callback(err, data);
    });
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
        console.log("json:" + item);
        if (item.installationId && item.tag && item.metric) {
            items.push(item);
        } else {
          console.log("Ignoring incorrect json");
        }
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

