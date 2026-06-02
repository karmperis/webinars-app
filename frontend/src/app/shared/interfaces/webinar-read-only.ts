import { UserReadOnly } from "./user-read-only";

/**
 * Represents a webinar returned by the backend API.
 */
export interface WebinarReadOnly {
uuid: string;
title: string;
description: string;
scheduledDate: string;
duration: number; 
organizer: UserReadOnly;
}