#!/usr/bin/env node

var util = require('util');

function getRandomInt(min, max) {
    return Math.floor(Math.random() * (max - min)) + min;
}

function getRandomDate() {
    var now = new Date();
    var old = new Date(+now - 12096e5)
    var rand = getRandomInt(+old, +now);
    var randomDate = new Date(rand);
    console.error(randomDate.toISOString());
    return randomDate.toISOString();
}

function getRandomMetric() {
    var index = getRandomInt(0, 2);
    var tag = ['temp', 'humidity'][index];
    var value = (tag === 'temp') ? getRandomInt(-40, 40) : getRandomInt(0, 100);
    return [tag, value];
}


function newMetric() {
    var tagValue = getRandomMetric();
    return {
        "timestamp": getRandomDate(),
        "installationId": "123123q",
        "tag": tagValue[0],
        "metric": tagValue[1]
    }
}

function encodedMetric() {
    var metric = newMetric();
    var json = JSON.stringify(metric, ' ');
    return new Buffer(json).toString('base64')
}


function newRecord() {
    return {
        "kinesis": {
            "partitionKey": "partitionKey-3",
            "kinesisSchemaVersion": "1.0",
            "data": encodedMetric(),
            "sequenceNumber": "49545115243490985018280067714973144582180062593244200961"
        },
        "eventSource": "aws:kinesis",
        "eventID": "shardId-000000000000:49545115243490985018280067714973144582180062593244200961",
        "invokeIdentityArn": "arn:aws:iam::059493405231:role/testLEBRole",
        "eventVersion": "1.0",
        "eventName": "aws:kinesis:record",
        "eventSourceARN": "arn:aws:kinesis:us-east-1:35667example:stream/examplestream",
        "awsRegion": "us-east-1"
    }
}

function newEvent() {
    var records = [];
    for (var i = 0; i < 20; i++) {
        records.push(newRecord());
    }
    return {
        "Records": records
    }
}

console.log(JSON.stringify(newEvent(), null, 2));
