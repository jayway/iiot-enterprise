# IIoT - Team Enterprise

## Grafana

[Grafana](http://grafana.org/) is used to visualize the data posted to InfluxDB.

Setting up Grafana locally is pretty straightforward, but for our purposes this assumes you have an Influx database ready to go against. But if you don't and want something to play with, you'll need to set it up for yourself:

```
brew install influxdb
```

And start it up:

```
influxdb -config=/usr/local/etc/influxdb.conf
```

**Note:** If you want to easily set up a remote database, you can do that [here](http://play.influxdb.org/), which is probably the case if you're posting to it from CloudWatch.

Navigate to `localhost:8083` and create your database and its entries.

Moving on; download Grafana from [here](http://grafana.org/download/):

* Unpack wherever you want.
* Rename `config.sample.js` to `config.js`.
* Enter your database credentials.
* Start up a web server (such as [live-server](https://www.npmjs.com/package/live-server)).
* Navigate to your new web server and go bananas.
