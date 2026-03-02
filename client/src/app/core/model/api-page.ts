/**
 * Defines the sort order used for the request
 */
class SortOrder {
    ascending!: boolean;
    descending!: boolean;
    direction!: string;
    ignoreCase!: boolean;
    nullHandling!: string;
    property!: string;
}

/**
 * Represents a page for listing objects.
 */
export class ApiPage<T> {

    /**
     * Contains the results of the API request
     */
    content!: T[];

    /**
     * Paging details
     */
    page!: Page;

}

export class Page {

  /**
   * The current page number
   */
  number!: number;

  /**
   * The page size
   */
  size!: number;

  /**
   * The total number of items
   */
  totalElements!: number;

  /**
   * The total number of pages
   */
  totalPages!: number;

  /**
   * The sort details
   */
  sort!: Array<SortOrder>;

}
