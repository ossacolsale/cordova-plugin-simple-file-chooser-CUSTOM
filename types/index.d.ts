declare class chooser {
    static open (accept: string, successCallback: ()=>any, failureCallback: ()=>any): Promise<FileChooserResult>;
    static open (successCallback: ()=>any, failureCallback: ()=>any): Promise<FileChooserResult>;
    static readFile (uri: string): Promise<string>;
}

interface FileChooserResult {
    mediaType: string;
    name: string;
    uri: string;
    content: string;
}
