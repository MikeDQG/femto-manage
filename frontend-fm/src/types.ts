export type Activity = {
    title: string;
    start: string;
    end: string;
    category: string;
    place: string;
    description: string;
};

export type Category = [name: string, goalMinutes: number, completedMinutes: number];
