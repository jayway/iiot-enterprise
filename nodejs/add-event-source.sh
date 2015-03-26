
role='arn:aws:iam::554360467205:role/lambda_exec_role'
region='eu-west-1'
stream='arn:aws:kinesis:eu-west-1:554360467205:stream/iot_lab_jens'

aws lambda add-event-source \
   --region $region \
   --function-name processMetrics \
   --role $role \
   --event-source $stream \
   --batch-size 100
