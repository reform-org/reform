export  const error = (message: string, fields: string[] = []) => {
    return {
        error: {
            message,
            fields
        }
    };
};