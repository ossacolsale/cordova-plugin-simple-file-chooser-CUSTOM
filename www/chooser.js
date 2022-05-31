let ReadFile = (uri) => new Promise((resolve, reject) => {
    cordova.exec(resolve, reject, "Chooser", "readFile", [uri]);
});

module.exports = {
    getFiles: function (accept, successCallback, failureCallback) {
        var result = new Promise(function (resolve, reject) {
            cordova.exec(
                function (json) {
                    try {
                        resolve(JSON.parse(json));
                    } catch (err) {
                        reject(err);
                    }
                },
                reject,
                'Chooser',
                'getFiles',
                [
                    (typeof accept === 'string'
                        ? accept.replace(/\s/g, '')
                        : undefined) || '*/*',
                ]
            );
        });

        if (typeof successCallback === 'function') {
            result.then(successCallback);
        }
        if (typeof failureCallback === 'function') {
            result.catch(failureCallback);
        }

        return result;
    },
    grantDir: function (startFile, successCallback, failureCallback) {
        var result = new Promise(function (resolve, reject) {
            cordova.exec(
                
                function (res) {
                    try {
                        resolve(res == 'ok');
                    } catch (err) {
                        reject(err);
                    }
                },
                reject,
                'Chooser',
                'grantDir',
                [
                    startFile
                ]
            );
        });

        if (typeof successCallback === 'function') {
            result.then(successCallback);
        }
        if (typeof failureCallback === 'function') {
            result.catch(failureCallback);
        }

        return result;
    },
    readFile: function (uri) {
        return ReadFile(uri)
            .catch(reason => {
                return Promise.reject(reason);
            });
    }
};