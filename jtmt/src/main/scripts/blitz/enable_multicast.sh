#!/bin/bash
sudo /sbin/ifconfig lo multicast
sudo /sbin/route add -net 224.0.0.0 netmask 240.0.0.0 dev lo
