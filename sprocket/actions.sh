#!/bin/sh

SPROCKET_DIR=`dirname $0`;

source $SPROCKET_DIR/conf.sh


case "$1" in

    "package" )

        if [ -d $SPROCKET_PACKAGE_DIR ]; then
            echo "Removing existing packaging directory... "
            rm -rf $SPROCKET_PACKAGE_DIR
        fi

        echo "Creating packaging directory... "
        mkdir $SPROCKET_PACKAGE_DIR

        echo "Copying one-jar into package directory... "
        cp `find ./ | grep -i one-jar\.jar` $SPROCKET_PACKAGE_DIR/analytics-engine.jar

        echo "Copying rules-files directory... "
        cp -R ./src/main/resources/rule-files $SPROCKET_PACKAGE_DIR

        echo "Replicating deployment bundle to destination... "
        rsync -ravP ./sprocket $SPROCKET_PACKAGE_DIR
    ;;


    "deploy" )
        
        echo "Installing upstart script... "
        sudo cp $SPROCKET_DIR/$SPROCKET_UPSTART /etc/init/$SPROCKET_APP_NAME.conf
        sudo chown root:root /etc/init/$SPROCKET_APP_NAME.conf

        echo "Installing monit script... "
        sudo cp $SPROCKET_DIR/$SPROCKET_MONIT /etc/monit/conf.d/$SPROCKET_APP_NAME.conf

        echo "Setting permissions on destination dir... "
        sudo chown -R $SPROCKET_CLOUD_USER:$SPROCKET_CLOUD_GROUP /data/$SPROCKET_APP_NAME

        echo "Restarting $SPROCKET_APP_NAME... "
        sudo stop $SPROCKET_APP_NAME
        sudo start $SPROCKET_APP_NAME

        echo "Restarting monit... "
        sudo /etc/init.d/monit restart

    ;;


    * )
    ;;

esac