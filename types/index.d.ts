declare class chooser {
    static getFiles (accept: string, successCallback: ()=>any, failureCallback: ()=>any): Promise<Array<FileChooserResult>>;
    static grantDir (startFile: string, successCallback: () => any, failureCallback: () => any): Promise<boolean>;
    static readFile (uri: string): Promise<string>;
}

interface FileChooserResult {
    mediaType: string;
    name: string;
    uri: string;
    content: string;
}
